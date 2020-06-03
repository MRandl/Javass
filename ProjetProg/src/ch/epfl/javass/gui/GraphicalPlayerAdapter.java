package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import static javafx.application.Platform.runLater;

public class GraphicalPlayerAdapter implements Player {
    private final ScoreBean sB;
    private final TrickBean tB;
    private final HandBean hB;
    private GraphicalPlayer gPlayer;
    private final ArrayBlockingQueue<Card> blockingQueue;

    public GraphicalPlayerAdapter(){
        this.sB = new ScoreBean();
        this.tB = new TrickBean();
        this.hB = new HandBean();
        blockingQueue = new ArrayBlockingQueue<>(1);
    }

    /**
     * @param state the current state of the game
     * @param hand the hand of the player
     * @return the card the human player has decided to play
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        Card c;
        runLater(() -> hB.setPlayableCards(state.trick().playableCards(hand))); //allow the player to play
        try {
            c = blockingQueue.take(); //wait for the player to click on a card
        } catch (InterruptedException e) {
            throw new IllegalStateException(); //crash if the blocking queue or thread is interrupted
        }
        runLater(() -> hB.setPlayableCards(CardSet.EMPTY)); // disable the player's ability to play

        return c;
    }

    /**
     * @param ownId the id of the current player
     * @param playerNames maps playerIds to the respective names of the players
     *    method creates the player and its associated window
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        gPlayer = new GraphicalPlayer(ownId, playerNames, sB, tB, hB, blockingQueue, 1.5);
        runLater(() -> gPlayer.createStage().show());
    }

    /**
     * updates the hand the player has access to
     * @param newHand the hand of the player
     */
    @Override
    public void updateHand(CardSet newHand) {
        runLater(() ->  hB.setHand(newHand));
    }

    /**
     * @param trump the new trump the player will see
     */
    @Override
    public void setTrump(Card.Color trump) {
        runLater(() -> tB.setTrump(trump));
    }

    /**
     * @param newTrick
     */
    @Override
    public void updateTrick(Trick newTrick) {
        runLater(() -> {tB.setTrick(newTrick); tB.setTrump(newTrick.trump());});
    }

    /**
     * @param score the new score the player will see
     */
    @Override
    public void updateScore(Score score) {
        runLater(() -> {for(TeamId team : TeamId.ALL) {
            sB.setTotalPoints(team, score.totalPoints(team));
            sB.setGamePoints(team, score.gamePoints(team));
            sB.setTurnPoints(team, score.turnPoints(team));
        }});
    }

    /**
     * @param winningTeam the team that won
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        runLater(() -> sB.setWinningTeam(winningTeam));
    }
}
