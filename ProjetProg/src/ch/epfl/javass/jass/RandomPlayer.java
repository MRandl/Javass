package ch.epfl.javass.jass;

import java.util.Random;

public final class RandomPlayer implements Player {
    private final Random rng;

    public RandomPlayer(long rngSeed) {
      this.rng = new Random(rngSeed);
    }

    /**
     * @param state the current turnstate
     * @param hand the current hand of the player
     * @return the card chosen at pseudo-random that the player will play
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
      CardSet playable = state.trick().playableCards(hand);
      return playable.get(rng.nextInt(playable.size()));
    }
    
    
  }