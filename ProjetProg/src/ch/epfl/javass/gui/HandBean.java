package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import static javafx.collections.FXCollections.*;

public final class HandBean {

    private ObservableList<Card> hand = observableArrayList();
    private ObservableSet<Card> playableCards = observableSet();

    HandBean() {
        for(int i = 0; i < 9; i++)
            hand.add(i,null);
    }

    /**
     * @param newHand the new hand the player can play from
     */
    void setHand(CardSet newHand){
        if(newHand.size() == 9)
            for (int i = 0; i < newHand.size(); ++i)
                hand.set(i, newHand.get(i));
        else
            for(int i = 0; i < hand.size(); ++i)
                if(!(hand.get(i) == null || newHand.contains(hand.get(i))))
                    hand.set(i, null);
    }

    /**
     * @return the hand of the player
     */
    ObservableList<Card> hand() {
        return unmodifiableObservableList(hand);
    }

    /**
     * @param newPlayableCards the playable cards among the hand set
     */
    void setPlayableCards(CardSet newPlayableCards){
        if(playableCards.isEmpty())
            for(int i = 0; i < newPlayableCards.size(); i++)
                playableCards.add(newPlayableCards.get(i));
        else
            playableCards.removeIf(c -> !(newPlayableCards.contains(c)));
    }

    /**
     * @return the playable cards of the player
     */
    ObservableSet<Card> playableCards() {
        return unmodifiableObservableSet(playableCards);
    }
}