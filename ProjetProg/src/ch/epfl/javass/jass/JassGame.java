package ch.epfl.javass.jass;

import java.util.*;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public final class JassGame {
    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {

        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());
        //initializes the random number generators

        this.score = Score.INITIAL;
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.names = Collections.unmodifiableMap(new EnumMap<>(playerNames));
        //makes a safe copy of the maps
    }

    private Map<PlayerId, String> names;
    private Map<PlayerId, Player> players;

    private Score score;
    private long[] cards = new long[4];
    private Random shuffleRng;
    private Random trumpRng;
    private TurnState turnstate;
    private Color currentTrump;
    private PlayerId playerToBegin = PlayerId.PLAYER_1;

    private boolean isNewGame = true;
    //makes it easier and uses very few memory slots since there is
    //only one jassgame instantiated


    //i recommend reading advanceToEndOfNextTrick right now, since it uses all of the methods below
    //it's the last method
    /**
     * @return true if a team has won, false otherwise
     */
    public boolean isGameOver() {
        return  score.totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS ||
                score.totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS;
    }


    /**
     * shuffles the cards and distributes them to the players
     */
    private void deckShuffler() {
        List<Card> deck = new LinkedList<>();
        for(int card = 0; card < Color.COUNT * Rank.COUNT; ++card)
                deck.add(CardSet.ALL_CARDS.get(card));
        //deck is a linkedList that contains all the ordered cards

        Collections.shuffle(deck, shuffleRng);
        for(short i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i)
            cards[i] = CardSet.of(deck.subList(i*Jass.HAND_SIZE,(i+1)*Jass.HAND_SIZE)).packed();
        //shuffles and puts them in cards, the array of the hands
    }

    /**
     * determines the next trump and stores it in currentTrump;
     */
    private void trumpShuffle() {
        currentTrump = Color.ALL.get(trumpRng.nextInt(4));
    }

    /**
     * do i have to explain this one
     */
    private void updateTrickForEverybody() {
        for(PlayerId play : PlayerId.ALL) {
            players.get(play).updateTrick(turnstate.trick());
        }
    }

    /**
     * creates a new game ready to be played
     */
    private void setupGame() {
        deckShuffler();
        trumpShuffle();
        //shuffles everything
        for(short i = 0; i < 4; ++i)  //determines the player that will play first
            if((cards[i] & PackedCardSet.singleton(Card.of(Color.DIAMOND, Rank.SEVEN).packed())) != 0)
                playerToBegin = PlayerId.ALL.get(i);

        this.turnstate = TurnState.initial(currentTrump, score, playerToBegin);

        for(PlayerId play : PlayerId.ALL) { //sets everything for the players
            players.get(play).setPlayers(play, names);
            players.get(play).updateHand(CardSet.ofPacked(cards[PlayerId.ALL.indexOf(play)]));
            players.get(play).setTrump(currentTrump);
            players.get(play).updateScore(score);
            players.get(play).updateTrick(turnstate.trick());
        }
    }

    /**
     * called when the trick is full, empties it and updates everyone about it
     */
    private void goToNewTrick() {
        turnstate = turnstate.withTrickCollected();

        score = turnstate.score();
        if(!turnstate.isTerminal()) {
            for (PlayerId play : PlayerId.ALL) {
                //updates the score for everybody when the turnstate isn't terminal
                //(not every time, to avoid calling it twice when the turnstate is updated)
                players.get(play).updateScore(score);
            }
            updateTrickForEverybody();
        }
    }

    /**
     * called when the turnstate is terminal, saves the score and creates a new one
     */
    private void generateNewTurnSet() {
        deckShuffler();
        trumpShuffle();
        //shuffles everything for the next turnstate

        this.playerToBegin = PlayerId.ALL.get((PlayerId.ALL.indexOf(playerToBegin)+1)%4);
        //updates the next player

        turnstate = TurnState.initial(currentTrump, score.nextTurn(), playerToBegin);
        //updates the turnstate

        for(PlayerId play : PlayerId.ALL) {
            //updates the trump, score and trick for everyone
            players.get(play).setTrump(currentTrump);
            players.get(play).updateScore(score.nextTurn());
            players.get(play).updateTrick(turnstate.trick());
            players.get(play).updateHand(CardSet.ofPacked(cards[play.ordinal()]));
        }
    }


    /**
     * calls the cardToPlay for everybody until the current trick is full
     */
    private void playersPlay() {

        while(!turnstate.trick().isFull()) {

            PlayerId p = turnstate.nextPlayer();
            Card c = players.get(p).cardToPlay(turnstate, CardSet.ofPacked(cards[p.ordinal()]));
            //determines the next player and the card he wants to play

            players.get(p).updateHand(CardSet.ofPacked(cards[PlayerId.ALL.indexOf(p)]).remove(c));
            cards[PlayerId.ALL.indexOf(p)] = PackedCardSet.remove(cards[PlayerId.ALL.indexOf(p)], c.packed());
            turnstate = turnstate.withNewCardPlayed(c);
            //plays that card, and removes it from the hand of the player

            updateTrickForEverybody();
        }

    }

    /**
     * @return true if the game is over, and sets the winning team if so, and returns false otherwise
     */
    private boolean ifGameOverThenEndTheGame() { //wrapper for isGameOver(), not to be confused with it

        //since that if statement just below was called twice inside of advanceToEndOfNextTrick() (every time we
        //check if the game is over) it now has its own method for clarity and maintainability
        if(isGameOver()) {
            for(PlayerId p: PlayerId.ALL){
                players.get(p).setWinningTeam(turnstate.score().totalPoints(TeamId.TEAM_1) >
                        turnstate.score().totalPoints(TeamId.TEAM_2) ?
                        TeamId.TEAM_1 : TeamId.TEAM_2);
                //when the game is over, sets the winning team for everybody and returns true
                //note that this only happens once so the players aren't updated twice
            }
            return true;
        }
        return false;
    }


    /**
     * called while the game isn't over
     */
    public void advanceToEndOfNextTrick() {
        //based off of a piazza answer that described the behavior of that method

        if (ifGameOverThenEndTheGame())  //advanceToEnd...() does nothing if the game is over
            return;                      //and if so players are notified by ifGameOver...()


        if(isNewGame) { //setups the game if it is the first time it is played
            isNewGame = false;
            setupGame();

        } else { //assume the trick is full otherwise
            goToNewTrick();
        }

        if(turnstate.isTerminal())
            //if the trick has reached the limit of indices, create a new turnstate
            generateNewTurnSet();

        if (ifGameOverThenEndTheGame())
            //same as before
            return;

        playersPlay(); //players play until the trick is full

    }

}
