package ch.epfl.javass.jass;

import java.util.List;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * @author Mathis Randl
 *
 */
public final class CardSet {

    private CardSet(long pk) {
        checkArgument(PackedCardSet.isValid(pk));
        this.pkCardSet = pk;
    }

    private long pkCardSet;
    
    public final static CardSet EMPTY = 
            new CardSet(PackedCardSet.EMPTY);
    
    public final static CardSet ALL_CARDS = 
            new CardSet(PackedCardSet.ALL_CARDS);

    /**
     * @param cards
     *            a list (util.List) of cards that will belong to the current
     *            set
     * @return the constructed set of cards
     */
    public static CardSet of(List<Card> cards) {
        long finalCardSetValue = PackedCardSet.EMPTY;
        for (Card card : cards) {
            finalCardSetValue = PackedCardSet.add(finalCardSetValue, card.packed());
                // iterates through the list and adds them one 
                // by one to finalCardSetValue
        }
        return new CardSet(finalCardSetValue);
    }

    /**
     * @param packed
     *            a packed card set
     * @return the constructed set of cards
     */
    public static CardSet ofPacked(long packed) {
        return new CardSet(packed);
    }

    /**
     * @return the packed version of the set
     */
    public long packed() {
        return pkCardSet;
    }

    /**
     * @return true if the card set is empty, false in all the other cases
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(pkCardSet);
    }

    /**
     * @return the number of cards in the set
     */
    public int size() {
        return PackedCardSet.size(pkCardSet);
    }

    /**
     * @param index
     *            a number between 0 included and this.size() excluded
     * @return the card at the position of the index-th "1" in the two-bit's
     *         complement representation of the current set
     */
    public Card get(int index) {
        return Card.ofPacked(PackedCardSet.get(pkCardSet, index));
    }

    /**
     * @param card
     *            a card to be added to the current instance of CardSet
     * @return a set of card that contains the elements of the current set, and
     *         the Card card
     */
    public CardSet add(Card card) {
        return ofPacked(PackedCardSet.add(pkCardSet, card.packed()));
    }

    /**
     * @param card
     *            a card to be removed to the current instance of CardSet
     * @return a set of card that contains the elements of the current set,
     *         without the Card card
     */
    public CardSet remove(Card card) {
        return ofPacked(PackedCardSet.remove(pkCardSet, card.packed()));
    }

    /**
     * @param card
     *            a card
     * @return true if card is in the set, false in all other cases
     */
    public boolean contains(Card card) {
        return PackedCardSet.contains(pkCardSet, card.packed());
    }

    /**
     * @return the opposite set of the current set of Cards
     */
    public CardSet complement() {
        return ofPacked(PackedCardSet.complement(pkCardSet));
    }

    /**
     * @param that
     *            another set of cards
     * @return the union of the current set and that
     */
    public CardSet union(CardSet that) {
        return ofPacked(PackedCardSet.union(this.packed(), that.packed()));
    }

    /**
     * @param that
     *            another set of cards
     * @return the intersection of the current set and that
     */
    public CardSet intersection(CardSet that) {
        return ofPacked(PackedCardSet.intersection(this.packed(), that.packed()));
    }

    /**
     * @param that
     *            another set of cards
     * @return the difference of the current set and that
     */
    public CardSet difference(CardSet that) {
        return ofPacked(PackedCardSet.difference(this.packed(), that.packed()));
    }

    /**
     * @param color
     *            a color
     * @return the set of cards that contains all cards of the color color
     */
    public CardSet subsetOfColor(Card.Color color) {
        return ofPacked(PackedCardSet.subsetOfColor(pkCardSet, color));
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CardSet) && this.packed() == ((CardSet) o).packed();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(packed());
    }

    @Override
    public String toString() {
        return PackedCardSet.toString(packed());
    }
}
