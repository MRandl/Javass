package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

import static ch.epfl.javass.bits.Bits32.extract;

/**
 * @author Mathis Randl
 *
 */
public final class PackedCard {

    private PackedCard() {
    }

    public static int INVALID = 0b111111;

    private static final int RANK_INDEX = 0;
    private static final int RANK_SIZE = 4;
    private static final int COLOR_INDEX = 4;
    private static final int COLOR_SIZE = 2;

    /**
     * @param pkCard
     *            a packed card
     * @return true if pkCard is a valid card, false if it is not
     */
    public static boolean isValid(int pkCard) {
        return (extract(pkCard, RANK_INDEX, RANK_SIZE) <= 0b1000) &&
                (pkCard >>> (RANK_SIZE + COLOR_SIZE) == 0);
    }

    /**
     * @param c
     *            a color
     * @param r
     *            a rank
     * @return the packed card that corresponds to the color c and rank r
     */
    public static int pack(Color c, Rank r) {
        return Bits32.pack(Rank.ALL.indexOf(r),  RANK_SIZE,
                           Color.ALL.indexOf(c), COLOR_SIZE);
    }

    /**
     * @param pkCard
     *            a packed card
     * @return the color of pkCard
     */
    public static Color color(int pkCard) {
        return Color.ALL.get(extract(pkCard, COLOR_INDEX, COLOR_SIZE));
    }

    /**
     * @param pkCard
     *            a packed card
     * @return the rank of pkCard
     */
    public static Rank rank(int pkCard) {
        return Rank.ALL.get(extract(pkCard, RANK_INDEX, RANK_SIZE));
    }

    /**
     * @param trump
     *            the trump in that current round
     * @param pkCardL
     *            a packed card
     * @param pkCardR
     *            another packed card
     * @return true if pkCardL is comparable to pkCardR and is a better card,
     *         false else
     */
    public static boolean isBetter(Color trump, int pkCardL, int pkCardR) {
        if (color(pkCardL) != color(pkCardR))
            return color(pkCardL) == trump;
        //if they don't have the same color, returns true iff the first card is a trump

        if (trump == color(pkCardL)) //they have the same color at this point,
            //so now is the case they are both trump
            return rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal();
        else
            //they are both not trump but still the same color
            return rank(pkCardL).ordinal() > rank(pkCardR).ordinal();

    }

    /**
     * @param trump
     *            the trump in that current round
     * @param pack
     *            a packed card
     * @return the points that card is worth
     */
    public static int points(Color trump, int pack) {
        int[] valueNotTrump = new int[]{0, 0, 0, 0, 10, 2, 3, 4, 11};
        int[] valueTrump = new int[]{0, 0, 0, 14, 10, 20, 3, 4, 11};

        return color(pack) == trump
                ? valueTrump[extract(pack, RANK_INDEX, RANK_SIZE)]
                : valueNotTrump[extract(pack, RANK_INDEX, RANK_SIZE)];
    }

    public static String toString(int pkCard) {
        return rank(pkCard).toString() + color(pkCard).toString();
    }
}
