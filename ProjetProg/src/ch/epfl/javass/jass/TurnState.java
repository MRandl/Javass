package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

public final class TurnState {

    private TurnState(long score, long unplayed, int trick) {
        this.currentPkScore = score;
        this.unplayedPkCardSet = unplayed;
        this.currentPkTrick = trick;
    }
    
    private long currentPkScore;
    private long unplayedPkCardSet;
    private int currentPkTrick;

    /**
     * @param trump the trump color that was selected for this turnstate
     * @param score the score at which the turnstate should begin
     * @param firstPlayer the first player to play
     * @return a turnstate corresponding to these specifications
     */
    public static TurnState initial(Color trump, Score score, PlayerId firstPlayer) {
        return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * @param pkScore the packed score at which the turnstate should begin
     * @param pkUnplayedCards the packed card set of the cards not played until now
     * @param pkTrick the packed trick at which the turnstate begins
     * @return a turnstate corresponding to these specifications
     */
    public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
        Preconditions.checkArgument(PackedScore.isValid(pkScore) && PackedCardSet.isValid(pkUnplayedCards) && PackedTrick.isValid(pkTrick));
        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }

    /**
     * @return the current packed score
     */
    public long packedScore() {
        return currentPkScore;
    }

    /**
     * @return the packed card set corresponding to the unplayed cards of the current turnstate
     */
    public long packedUnplayedCards() {
        return unplayedPkCardSet;
    }

    /**
     * @return the packed trick of the current turnstate
     */
    public int packedTrick() {
        return currentPkTrick;
    }

    /**
     * @return the score of the current turnstate
     */
    public Score score() {
        return Score.ofPacked(currentPkScore);
    }

    /**
     * @return the card set corresponding to the unplayed cards of the current turnstate
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(unplayedPkCardSet);
    }

    /**
     * @return the trick of the current turnstate
     */
    public Trick trick() {
        return Trick.ofPacked(currentPkTrick);
    }

    /**
     * @return true iff the turnstate is terminal, i.e. all 8 tricks were played
     */
    public boolean isTerminal() {
        return this.currentPkTrick == PackedTrick.INVALID;
    }

    /**
     * @return the player that has to play next
     * @throws IllegalStateException if the trick is currently full
     */
    public PlayerId nextPlayer() {
        if(PackedTrick.isFull(currentPkTrick)) throw new IllegalStateException();
        return PackedTrick.player(currentPkTrick, PackedTrick.size(currentPkTrick));
    }

    /**
     * @param card the card that will be played
     * @return the turnstate corresponding to the current turnstate, with a new card played
     */
    public TurnState withNewCardPlayed(Card card) {
        if(PackedTrick.isFull(currentPkTrick)) throw new IllegalStateException();
        return new TurnState(currentPkScore, PackedCardSet.remove(unplayedPkCardSet, card.packed()), PackedTrick.withAddedCard(currentPkTrick, card.packed()));
    }

    /**
     * @return the turnstate corresponding to the current turnstate, but the full trick it contains was collected
     */
    public TurnState withTrickCollected() {
        if(!PackedTrick.isFull(currentPkTrick)) throw new IllegalStateException();
        return new TurnState(PackedScore.withAdditionalTrick(currentPkScore, PackedTrick.winningPlayer(currentPkTrick).team(), PackedTrick.points(currentPkTrick)), unplayedPkCardSet, PackedTrick.nextEmpty(currentPkTrick));
    }

    /**
     * @param card the card that will be played
     * @return the turnstate corresponding to the current turnstate, with a new card played and if
     * possible, a full trick collected
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        TurnState tmp = this.withNewCardPlayed(card);
        if(PackedTrick.isFull(tmp.packedTrick()))
            return tmp.withTrickCollected();
        return tmp;
        
    }
    
    
    
    
    
}