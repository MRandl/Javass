package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public final class TrickBean {

    private SimpleObjectProperty<Card.Color> trump = new SimpleObjectProperty<>();
    private ObservableMap<PlayerId, Card> trick = FXCollections.observableHashMap();
    private SimpleObjectProperty<PlayerId> winningPlayer = new SimpleObjectProperty<>();

    /**
     * @param color the color that will be displayed in the middle
     */
    public void setTrump(Card.Color color){
        trump.set(color);
    }

    /**
     * @return the current trump displayed
     */
    public ReadOnlyObjectProperty<Card.Color> getTrump(){
        return trump;
    }

    /**
     * @param t the trick that will be displayed
     */
    public void setTrick(Trick t){
        for(short i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i){
            PlayerId p = t.player(i);
            if(t.size() > i) {
                trick.put(p, t.card(i));
            } else {
                trick.put(p, null);
            }
        }
        winningPlayer.set(t.size() == 0 ? null : t.winningPlayer());
    }

    /**
     * @return the trick that is currently displayed
     */
    public ObservableMap<PlayerId, Card> getTrick() {
        return trick;
    }

    /**
     * @return the current winning player of the game
     */
    public SimpleObjectProperty<PlayerId> getWinningPlayer() {
        return winningPlayer;
    }
}
