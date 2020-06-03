package ch.epfl.javass.jass;

import java.util.Arrays;

import java.util.Collections;
import java.util.List;
import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * @author Mathis Randl
 *
 */
public final class Card {

    private final int PkdValue;

    /**
     * @param c
     *            the color of the card that will be generated
     * @param r
     *            the rank of the card that will be generated
     */
    private Card(Color c, Rank r) {
        PkdValue = PackedCard.pack(c, r);
    }

    /**
     * @param c
     *            the color of the card that will be generated
     * @param r
     *            the rank of the card that will be generated
     * @return a constructed card
     */
    public static Card of(Color c, Rank r) {
        return new Card(c, r);
    }

    /**
     * @param packed
     *            the packed card that will be used as a model
     * @return the card it corresponds to
     */
    public static Card ofPacked(int packed) {
        checkArgument(PackedCard.isValid(packed));
        return new Card(PackedCard.color(packed), PackedCard.rank(packed));
    }

    /**
     * @return the compact version of the current card
     */
    public int packed() {
        return PkdValue;
    }

    /**
     * @return the color of the current card
     */
    public Color color() {
        return PackedCard.color(PkdValue);
    }

    /**
     * @return the rank of the current card
     */
    public Rank rank() {
        return PackedCard.rank(PkdValue);
    }

    /**
     * @param trump
     *            the trump color during that fold
     * @param that
     *            the card to be compared to
     * @return true if the current card is better, false if they can't be
     *         compared or current card is weaker
     */
    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, this.packed(), that.packed());
    }

    /**
     * @param trump
     *            the trump color during that fold
     * @return the points the current card is worth
     */
    public int points(Color trump) {
        return PackedCard.points(trump, this.packed());
    }

    @Override
    public boolean equals(Object thatO) {
        return (thatO instanceof Card) && this.packed() == ((Card) thatO).packed();
    }

    @Override
    public int hashCode() {
        return this.packed();
    }

    @Override
    public String toString() {
        return PackedCard.toString(packed());
    }

    public enum Color {

        SPADE("\u2660"), HEART("\u2665"), DIAMOND("\u2666"), CLUB("\u2663");
            // they carry their characters as constructor arguments to avoid having
            // conditional branches later

        private final String character;

        Color(String c) {
            this.character = c;
        }

        public final static List<Color> ALL = 
            Collections.unmodifiableList(Arrays.asList(values()));

        public final static int COUNT = 4;

        @Override
        public String toString() {
            return character;
        }
    }

    public enum Rank {

        SIX("6", 0), SEVEN("7", 1), EIGHT("8", 2), NINE("9", 7), TEN("10",
                3), JACK("J", 8), QUEEN("Q", 4), KING("K", 5), ACE("A", 6);
                // same thing for the ranks, with the addition of
                // their trump ordinal, for similar reasons

        private final String character;
        private final int trumpO;

        Rank(String c, int trumpO) {
            this.character = c;
            this.trumpO = trumpO;
        }

        public final static List<Rank> ALL = 
            Collections.unmodifiableList(Arrays.asList(values()));

        public final static int COUNT = 9;

        @Override
        public String toString() {
            return character;
        }

        public int trumpOrdinal() {
            return trumpO;
        }
    }
}