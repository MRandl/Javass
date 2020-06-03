package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

public final class PacedPlayer implements Player {

    private Player underlyingPlayer;
    private double minTime;

    public PacedPlayer(Player underlyingPlayer, double minTime) {
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = 1000 * minTime;
    }

    /**
     * @param state the turnstate the player has to evolve in
     * @param hand the hand available to him
     * @return the card he plays
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long begin = System.currentTimeMillis();
        Card result = underlyingPlayer.cardToPlay(state, hand);
        //stores the result
        long end = System.currentTimeMillis();

        if(end-begin < minTime) { //sleeps if the player plays too fast
            try {
                Thread.sleep((long) (minTime - (end - begin)));
            } catch (InterruptedException e) {
                e.printStackTrace(); //no clue what that does but it makes intellij happy
            }
        }
        return result;
    }
    //All of the other methods only call the ones of the underlying player
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        underlyingPlayer.setPlayers(ownId, playerNames);
    }

    public void updateHand(CardSet newHand) {
        underlyingPlayer.updateHand(newHand);
    }

    public void setTrump(Color trump) {
        underlyingPlayer.setTrump(trump);
    }

    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }

    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }

    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }

}
