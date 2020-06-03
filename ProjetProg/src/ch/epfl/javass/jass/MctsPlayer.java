package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class MctsPlayer implements Player{
    // oh boi, here we go

    /**
     * @param ownId the PlayerId of the current player
     * @param rngSeed the seed used for all rng inside the player
     * @param iterations the number of mcts iterations
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        if(iterations < Jass.HAND_SIZE)
            throw new IllegalArgumentException();

        ownName = ownId;
        rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }

    private PlayerId ownName;
    private SplittableRandom rng;
    private int iterations;

    private class Node{ //cannot be static because I use ownName inside of this class

        private TurnState ts;
        private Node[] children;
        private int bigS = 0;
        private int bigN = 0;
        private int numberOfChildren = 0;
        private CardSet playableCards;
        private PlayerId PlayerThatPlayedThisNode;
        private CardSet hand;


        /**
         * @param t the turnstate the node represents
         * @param hand the hand of the current player
         * @param player the player that played the card that led to this node
         */
        private Node(TurnState t, CardSet hand, PlayerId player){
            ts = t; //t is always a new turnstate, so no access safety problems here

            this.hand = hand;

            if(ts.trick().isFull())
                ts = ts.withTrickCollected(); //stored turnstate cannot be full

            PlayerId next = ts.nextPlayer(); //next is the next player to play

            if(next == ownName) //that condition is the reason why node is not static (not a big deal tho)
                playableCards = ts.trick().playableCards(hand.intersection(ts.unplayedCards()));
            else
                playableCards = ts.unplayedCards().difference(hand);
            //playableCards represents the cards the next player will be able to play

            children = new Node[playableCards.size()];

            PlayerThatPlayedThisNode = player;
        }

        /**
         * adds a son to the current node, assumes it is possible
         * @return the son that was added
         */
        private Node AddASon(){
            PlayerId sonPlayer = ts.nextPlayer(); //ts is never full by construction (see Node constructor)
            Node k = new Node(ts.withNewCardPlayed(playableCards.get(numberOfChildren)), hand, sonPlayer);
            //k is the son of the current instance that represents
            //the smallest playable card that was not played yet

            children[numberOfChildren] = k; //place it where it belongs
            ++numberOfChildren; //current instance of node gained a son
            return k;
        }


        /**
         * @param c the variable c in the value computation formula
         * @return the index of the best son of the current node
         */
        private int selectBestSon(int c) {
            if(numberOfChildren == 0) {
                return -1; //returns -1 when the node has no children
            }
            int indexOfBest = 0; //by default the best son is the first
            for(int i = 0; i < numberOfChildren; ++i){
                if(sonValue(c,indexOfBest) < sonValue(c,i)) {
                    indexOfBest = i;
                }
            } //iterates through all of the children and finds the best
            return indexOfBest;
        }


        /**
         * @param c the c variable in the value-computation formula
         * @param i the index of the son to be evaluated
         * @return the value of the son at index i
         */
        private double sonValue(double c, int i) {
            if(children[i] == null)
                return -1;
            if(bigN == 0) //avoids log(0), because this method computes the son's value and not the current node's,
                // therefore it adds this problem. As a trade-off, i don't have to pay attention to that
                // +infinity value in the formula, it never happens
                return ((double)children[i].bigS/(double)children[i].bigN);

            return ((double)children[i].bigS/(double)children[i].bigN) + c * Math.sqrt((2*Math.log(bigN))/children[i].bigN);
            //computes the value as mentioned in the specifications
        }

        /**
         * looks for an optimal place to create a node and adds a node there
         * @return the arrayList of nodes between the original father and the
         * created node, first one excluded and last one included
         */
        private List<Node> addValidDescendant(){
            boolean mustContinue = true;
            List<Node> familyTree = new ArrayList<>();
            Node currentFather = this;

            while(mustContinue) { //while the right place has not been found
                if (currentFather.numberOfChildren < currentFather.playableCards.size()) {
                    //adds a son and stops the loop if the current node doesn't have all of its children
                    Node l = currentFather.AddASon();
                    familyTree.add(l);
                    mustContinue = false;

                } else if (currentFather.playableCards.size() == 0) {
                    //stops if we reach the bottom
                    break;

                } else {
                    //adds the current father to the list and keeps going
                    currentFather = currentFather.children[currentFather.selectBestSon(40)];
                    familyTree.add(currentFather); //note that the very first node is not in the list,
                                           //we don't care about that one since we'll only call that method on
                                           //the node representing the current turnstate, which doesn't need updates
                }
            }
            return familyTree;
        }
    }

    /**
     * @param n the node to be evaluated
     * @param hand the hand available
     * @return a score of the game, randomly played from the node
     */
    private Score randomlyEvaluate(Node n, CardSet hand) {
        TurnState tempTs = n.ts; //gets the current turnstate

        CardSet ownPlayerHand = hand.difference(n.ts.unplayedCards().complement());
        CardSet playableOther = tempTs.unplayedCards().difference(hand);
        //determines the cards playable by both the current player and the others
        //we can't easily use n.playableCards here, because it would create branching
        //and cause perf issues

        while (!tempTs.isTerminal()) {

            Card cardToPlay;

            if (tempTs.nextPlayer() == ownName) {
                //determines the playable cards, the played card and updates the hand when
                //the player is the current instance of the player
                CardSet playable = tempTs.trick().playableCards(ownPlayerHand);
                cardToPlay = playable.get(rng.nextInt(playable.size()));
                ownPlayerHand = ownPlayerHand.remove(cardToPlay);
            } else {
                //determines the playable cards, the played card and updates the hand when
                //the player is not the current instance of the player
                CardSet playable = tempTs.trick().playableCards(playableOther);
                cardToPlay = playable.get(rng.nextInt(playable.size()));
                playableOther = playableOther.remove(cardToPlay);
            }

            tempTs = tempTs.withNewCardPlayedAndTrickCollected(cardToPlay);
            //plays the card and collects the trick when needed
        }
        return tempTs.score();
    }

    /**
     * @param state the current state the player has to play to
     * @param hand the hand available to him
     * @return the card that he plays
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        Node father = new Node(state, hand, PlayerId.ALL.get((PlayerId.ALL.indexOf(ownName)+3)%4));
        // adds a new node played by the player to the left (just so that the sons are marked as played by the current one)
        //(+3 and not -1 because in java negative modulo values are not equal to the mathematically correct version)
        for(int i = 0; i < iterations; ++i) {
            List<Node> familyTree = father.addValidDescendant();//adds a descendant (will happen iterations times)

            Node addedNode = familyTree.get(familyTree.size()-1);
            Score nodeScore = randomlyEvaluate(addedNode, hand); //evaluates that descendant

            for(Node node : familyTree){ //propagates the values computed
                node.bigS += nodeScore.totalPoints(node.PlayerThatPlayedThisNode.team());
                ++node.bigN;
            }
        }
        //returns the card that was played by the best son after all of the computations
        return father.ts.unplayedCards().difference(father.children[father.selectBestSon(0)].ts.unplayedCards()).get(0);
    }
}
