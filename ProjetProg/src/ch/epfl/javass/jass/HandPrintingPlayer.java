package ch.epfl.javass.jass;

import java.util.Map;

public class HandPrintingPlayer implements Player{
    private final Player underlyingPlayer;
    private PlayerId ownId;
    public HandPrintingPlayer(Player underlyingPlayer){
        this.underlyingPlayer = underlyingPlayer;
    }
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        return underlyingPlayer.cardToPlay(state, hand);
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
        underlyingPlayer.setPlayers(ownId, playerNames);
    }

    @Override
    public void updateHand(CardSet newHand) {
        System.out.println("je suis joueur " + ownId + " et ma main est " + newHand);
        underlyingPlayer.updateHand(newHand);
    }

    @Override
    public void setTrump(Card.Color trump) {
        underlyingPlayer.setTrump(trump);
    }

    @Override
    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }

    @Override
    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }
}
