package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

import java.util.StringJoiner;
import java.util.stream.IntStream;

/**
 * @author Mathis Randl
 *
 */
public final class PackedCardSet {
    private PackedCardSet() {
    }

    public static final long EMPTY = 0L;
    public static final long ALL_CARDS = 0x01FF_01FF_01FF_01FFL;
    private static final int NUMBER_OF_COLORS = 4;
    private static final int NUMBER_OF_RANKS = 9;
    private static final int RANK_INDEX = 0;
    private static final int RANK_SIZE = 4;
    private static final int COLOR_INDEX = 4;
    private static final int COLOR_SIZE = 2;

    // 0x01FF == 0b0000000111111111, hexadecimal provides a compact form

    /*
     * Next method is computationally heavy but used only once, one could also have
     * used a hardcode directly for the array but i was told it was ugly. So
     * here we go
     */
    /**
     * @return the array that dictates what cards are better than another when
     *         they both are of the trump color
     */
    private final static long[][] generateTrumpAboveArray() {
        long[][] tmp = new long[NUMBER_OF_COLORS][NUMBER_OF_RANKS];
        // could have gone with only a [9] array and bitshift it as needed in
        // trumpAbove(), but this produces less computations if
        // that method is heavily used
        long iterator;
        for (int color = 0; color < NUMBER_OF_COLORS; ++color) {
            for (int rank = 0; rank < NUMBER_OF_RANKS; ++rank) {
                iterator = 0;
                // every card that is comparable gets compared to the card of
                // color color and rank rank
                // result is stored in iterator then saved in tmp[color][rank]
                for (int comparedRank = 0; comparedRank < NUMBER_OF_RANKS; ++comparedRank) {

                    iterator |= ((Card.Rank.ALL.get(comparedRank).trumpOrdinal() >
                                  Card.Rank.ALL.get(rank).trumpOrdinal() ? 1L : 0L)
                                        << (16 * color) + comparedRank);
                    //this line adds to the packedCardSet iterator a card if it is better than
                    //the card of color color and rank rank. When all cards have been compared to the current one,
                    //save iterator (line below) and do it again for the next card
                }
                tmp[color][rank] = iterator;
            }
        }
        return tmp;
    }

    // We can now store the result
    private final static long[][] trumpAboveArray = generateTrumpAboveArray();

    // Uses a hardcode on purpose because a generateSubsetOfColorArray()
    // method is as long in number of lines, but less clear
    private final static long[] subsetOfColorArray = new long[] {
            0x01FF,
            0x01FF  << 16,
            0x01FFL << 32,
            0x01FFL << 48 // See line 27 for an
            // explanation of the
            // value 0x01FFL
    };

    /**
     * @param pkCardSet
     *            a packed set of cards
     * @return true if the card set does not contain illegal cards, false else
     */
    public static boolean isValid(long pkCardSet) {
        return ((~ALL_CARDS & pkCardSet) == 0);
    }

    /**
     * @param pkCard
     *            a packed card
     * @return the packed set of cards that contains all the cards superior to
     *         pkCard, knowing its color is trump
     */
    public static long trumpAbove(int pkCard) {
        return trumpAboveArray[Bits32.extract(pkCard, COLOR_INDEX, COLOR_SIZE)]
                [Bits32.extract(pkCard, RANK_INDEX, RANK_SIZE)];
    }

    /**
     * @param pkCard
     *            a packed card
     * @return the packed set of cards that only contains pkCard
     */
    public static long singleton(int pkCard) {
        return Bits64.mask((Bits32.extract(pkCard, RANK_INDEX, RANK_SIZE) +
                            Bits32.extract(pkCard, COLOR_INDEX, COLOR_SIZE) * 16), 1);
        //the color tells in which quarter of the int the 1 must be placed (hence the *16), the rank in which position in that quarter
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @return true if the set is empty, false else
     */
    public static boolean isEmpty(long pkCardSet) {
        return pkCardSet == 0;
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @return the number of cards in it
     */
    public static int size(long pkCardSet) {
        return Long.bitCount(pkCardSet);
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @param index
     *            an integer between 0 included and size(pkCardSet) excluded
     * @return the packed card in pkCardSet of the index-th "1" bit
     */
    public static int get(long pkCardSet, int index) {

        int finalCardActualIndex = 0;
        for (int i = 0; i <= index; ++i) {

            finalCardActualIndex += Long.numberOfTrailingZeros(pkCardSet) + 1;
            pkCardSet >>= Long.numberOfTrailingZeros(pkCardSet) + 1;
            // removes all trailing 0's and one 1, index times,
            // and counts how much was deleted
        }
        return PackedCard.pack(Color.ALL.get(finalCardActualIndex / 16),
                Rank.ALL.get((finalCardActualIndex - 1) % 16));
        // returns the packed card with color index ranging from 0 to 3,
        // and rank ranging from 0 to 15 but only uses from 0 to 8
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @param pkCard
     *            a packed card
     * @return the packed card set that contains all of pkCardSet and pkCard
     */
    public static long add(long pkCardSet, int pkCard) {
        return pkCardSet | singleton(pkCard);
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @param pkCard
     *            a packed card
     * @return the packed card set that contains all of pkCardSet without pkCard
     */
    public static long remove(long pkCardSet, int pkCard) {
        return pkCardSet & ~singleton(pkCard);
    }

    /**
     * @param pkCardSet
     *            a packed card set
     * @param pkCard
     *            a card
     * @return true if card is in the set, false in all other cases
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        return (pkCardSet & singleton(pkCard)) != 0;
    }

    /**
     * @param pkCardSet
     * @return the opposite set of pkCardSet
     */
    public static long complement(long pkCardSet) {
        return ~pkCardSet & ALL_CARDS;
    }

    /**
     * @param pkCardSet1
     *            a packed set of cards
     * @param pkCardSet2
     *            another packed set of cards
     * @return the union of both sets
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 | pkCardSet2;
    }

    /**
     * @param pkCardSet1
     *            a packed set of cards
     * @param pkCardSet2
     *            another packed set of cards
     * @return the intersection of both sets
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 & pkCardSet2;
    }

    /**
     * @param pkCardSet1
     *            a packed set of cards
     * @param pkCardSet2
     *            another packed set of cards
     * @return the difference of both sets
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
        return intersection(pkCardSet1, complement(pkCardSet2));
    }

    /**
     * @param pkCardSet
     *            a packed set of cards
     * @param color
     *            a color
     * @return the packed set of cards that only contains cards that are of
     *         color color and are in the set
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        return pkCardSet & subsetOfColorArray[Card.Color.ALL.indexOf(color)];
        // since the array is private static it will be pre-generated 
        // and thus avoids us runtime computations
    }

    public static String toString(long pkCardSet) {
        StringJoiner j = new StringJoiner(",", "{", "}");
        IntStream.range(0, size(pkCardSet)).mapToObj(i -> PackedCard.toString(get(pkCardSet, i))).forEachOrdered(j::add);
        return j.toString();
    }
}
