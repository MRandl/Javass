package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

public interface Player {
    Card cardToPlay(TurnState state, CardSet hand);
    
    default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {}
    
    default void updateHand(CardSet newHand) {}
    
    default void setTrump(Color trump) {}
    
    default void updateTrick(Trick newTrick) {}
    
    default void updateScore(Score score) {}
    
    default void setWinningTeam(TeamId winningTeam) {}
    
}
