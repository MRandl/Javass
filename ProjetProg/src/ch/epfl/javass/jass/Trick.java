package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;
import static ch.epfl.javass.Preconditions.checkArgument;

public final class Trick {
    private Trick(int pk) {
        this.pkTrick = pk;
    }
    
    private int pkTrick;
    
    public final static Trick INVALID = new Trick(PackedTrick.INVALID);
    
    /**
     * @param trump the trump during that turn
     * @param firstPlayer the first player to play
     * @return an empty trick with the correct above assumptions
     */
    public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
        return ofPacked(PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * @param pkValue a packed trick
     * @return the corresponding trick
     * @throws IllegalArgumentException if pkValue is not valid
     */
    public static Trick ofPacked(int pkValue) {
        return new Trick(pkValue);
    }
    
    /**
     * @return the packed version of the current trick
     */
    public int packed() {
        return pkTrick;
    }
    
    /**
     * @return the trick naturally following our current trick
     */
    public Trick nextEmpty() {
        if(!(size() == 4)) throw new IllegalStateException();
        return ofPacked(PackedTrick.nextEmpty(pkTrick));
    }

    /**
     * @return the size of the current trick
     */
    public int size() {
        return PackedTrick.size(pkTrick);
    }
    
    /**
     * @return true if the current trick is empty, false otherwise
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(pkTrick);
    }
    
    /**
     * @return true if the current trick is full, false otherwise
     */
    public boolean isFull() {
        return PackedTrick.isFull(pkTrick);
    }
    
    /**
     * @return true if the current trick is the 
     * last of the turn, false otherwise
     */
    public boolean isLast() {
        return PackedTrick.isLast(pkTrick);
    }
    
    /**
     * @return the current trump color
     */
    public Color trump() {
        return PackedTrick.trump(pkTrick);
    }
    
    /**
     * @return the current index of the trick in the turn
     */
    public int index() {
        return PackedTrick.index(pkTrick);
    }
    
    /**
     * @param index an index
     * @return the player that will play index-th
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public PlayerId player(int index) {
        return PackedTrick.player(pkTrick, Preconditions.checkIndex(index, 4));
    }
    
    /**
     * @param index an index
     * @return the card that was played index-th during the trick
     */
    public Card card(int index) {
        return Card.ofPacked(PackedTrick.card(pkTrick, Preconditions.checkIndex(index, PackedTrick.size(pkTrick))));
    }
    
    /**
     * @param c a card
     * @return the trick that corresponds to the current trick
     * after card c was played
     */
    public Trick withAddedCard(Card c) {
        checkArgument(!isFull());
        return ofPacked(PackedTrick.withAddedCard(pkTrick, c.packed()));
    }
    
    /**
     * @return the base color of the current trick
     */
    public Color baseColor() {
        if(isEmpty())
            throw new IllegalStateException();
        return PackedTrick.baseColor(pkTrick);
    }
    
    /**
     * @param hand a cardset
     * @return the subset of hand that contains all of
     * the cards that can be played instantly
     */
    public CardSet playableCards(CardSet hand) {
       // if(isFull())
         //   throw new IllegalStateException();
        return CardSet.ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
    }
    
    /**
     * @return the points that trick is worth
     */
    public int points() {
        return PackedTrick.points(pkTrick);
    }
    
    /**
     * @return the player that currently wins the trick
     */
    public PlayerId winningPlayer() {
        if(isEmpty())
            throw new IllegalStateException();
        return PackedTrick.winningPlayer(pkTrick);
    }
    
   @Override
   public boolean equals(Object o) {
       if(o instanceof Trick) {
           return packed() == ((Trick) o).packed();
       }
       return false;
   }
   
   @Override 
   public int hashCode() {
       return packed();
   }
   
   @Override
   public String toString() {
       return PackedTrick.toString(pkTrick);
   }

}
