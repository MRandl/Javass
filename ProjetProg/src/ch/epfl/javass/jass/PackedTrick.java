package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits32.extract;
import static ch.epfl.javass.jass.PackedCardSet.ALL_CARDS;
import static ch.epfl.javass.jass.PackedCardSet.singleton;
import static ch.epfl.javass.jass.PackedCardSet.subsetOfColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public final class PackedTrick {
    private PackedTrick() {}

    public static final int INVALID = -1;

    private final static int MAX_TRICKS_NUMBER = 8;
    private final static int NUMBER_OF_NON_CARDS_BITS = 8;
    private final static int NO_CARDS = 0xFFFFFF;

    private final static int CARD_0_INDEX = 0;
    private final static int CARD_1_INDEX = 6;
    private final static int CARD_2_INDEX = 12;
    private final static int CARD_3_INDEX = 18;
    private final static int CARD_SIZE = 6;

    private final static int TRICK_POS = 24;
    private final static int TRICK_SIZE = 4;

    private final static int FIRST_PLAYER_POS = 28;
    private final static int FIRST_PLAYER_SIZE = 2;

    private final static int TRUMP_POS = 30;
    private final static int TRUMP_SIZE = 2;

    /**
     * @param pkTrick a packed trick
     * @return true if the packed trick is valid, false else
     */
    public static boolean isValid(int pkTrick) {
        boolean assertion =
                (Bits32.extract(pkTrick, TRICK_POS, TRICK_SIZE) < Jass.TRICKS_PER_TURN);

        int D = Bits32.extract(pkTrick, CARD_0_INDEX, CARD_SIZE);
        int C = Bits32.extract(pkTrick, CARD_1_INDEX, CARD_SIZE);
        int B = Bits32.extract(pkTrick, CARD_2_INDEX, CARD_SIZE);
        int A = Bits32.extract(pkTrick, CARD_3_INDEX, CARD_SIZE);

        assertion = assertion
                && (PackedCard.isValid(A) || A == PackedCard.INVALID)
                && (PackedCard.isValid(B) || B == PackedCard.INVALID)
                && (PackedCard.isValid(C) || C == PackedCard.INVALID)
                && (PackedCard.isValid(D) || D == PackedCard.INVALID);

        assertion = assertion
                &&((!PackedCard.isValid(C) || PackedCard.isValid(D))
                && (!PackedCard.isValid(B) || PackedCard.isValid(C))
                && (!PackedCard.isValid(A) || PackedCard.isValid(B)));
        // That block makes sure no valid card is followed by an invalid
        // card

        return assertion;
    }

    /**
     * @param trump a trump color
     * @param firstPlayer the player that gets to play first
     * @return a packed trick that contains no card and has the information provided above
     */
    public static int firstEmpty(Color trump, PlayerId firstPlayer) {
        return NO_CARDS | PlayerId.ALL.indexOf(firstPlayer) << FIRST_PLAYER_POS | Color.ALL.indexOf(trump) << TRUMP_POS;
    }

    /**
     * @param PkTrick the previous packed trick
     * @return the packed trick that is empty and follows PkTrick
     */
    public static int nextEmpty(int PkTrick) {

        if (extract(PkTrick, TRICK_POS,
                TRICK_SIZE) == MAX_TRICKS_NUMBER) {
            return INVALID;
        }

        int defaultValues = NO_CARDS;

        int winner = PlayerId.ALL.indexOf(winningPlayer(PkTrick));

        int index = (extract(PkTrick, TRICK_POS, TRICK_SIZE)+1);

        int trump = extract(PkTrick, TRUMP_POS, TRUMP_SIZE);

        return defaultValues | index << (TRICK_POS)
                | winner << (FIRST_PLAYER_POS)
                | trump << (TRUMP_POS);
        // could have used Bits32.pack() with seven arguments but well whatever, I find it much nicer that way
    }

    /**
     * @param pkTrick a packed trick
     * @return true if the packed trick is the ninth in the turn
     */
    public static boolean isLast(int pkTrick) {
        return extract(pkTrick, TRICK_POS, TRICK_SIZE) == MAX_TRICKS_NUMBER;
    }

    /**
     * @param pkTrick a packed trick
     * @return true if no cards were played
     */
    public static boolean isEmpty(int pkTrick) {
        return (pkTrick << NUMBER_OF_NON_CARDS_BITS) == (INVALID << NUMBER_OF_NON_CARDS_BITS);
    }

    /**
     * @param pkTrick a packed trick
     * @return if four cards were played
     */
    public static boolean isFull(int pkTrick) {
        return ((extract(pkTrick, CARD_0_INDEX, CARD_SIZE) != PackedCard.INVALID)
             && (extract(pkTrick, CARD_1_INDEX, CARD_SIZE) != PackedCard.INVALID)
             && (extract(pkTrick, CARD_2_INDEX, CARD_SIZE) != PackedCard.INVALID)
             && (extract(pkTrick, CARD_3_INDEX, CARD_SIZE) != PackedCard.INVALID));
    }

    /**
     * @param pkTrick a packed trick
     * @return the number of cards that were played in the trick
     */
    public static int size(int pkTrick) {
        for (int i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i) {
            if (extract(pkTrick, CARD_SIZE * i, CARD_SIZE) == PackedCard.INVALID)
                return i;
        }
        return 4;
    }

    /**
     * @param pkTrick a packed trick
     * @return the trump color of that packed trick
     */
    public static Color trump(int pkTrick) {
        return Color.ALL.get(extract(pkTrick, TRUMP_POS, TRUMP_SIZE));
    }

    /**
     * @param pkTrick a packed trick
     * @param index an integer
     * @return the index-th player to play in that trick
     */
    public static PlayerId player(int pkTrick, int index) {
        return PlayerId.ALL.get((extract(pkTrick, FIRST_PLAYER_POS,
                FIRST_PLAYER_SIZE) + index) % 4);
    }

    /**
     * @param pkTrick a packed trick
     * @return the index of that trick (0-8)
     */
    public static int index(int pkTrick) {
        return extract(pkTrick, TRICK_POS, TRICK_SIZE);
    }

    /**
     * @param pkTrick a packed trick
     * @param index an index (0 - size(pkTrick))
     * @return the packed card that is placed at position index in the packed trick
     */
    public static int card(int pkTrick, int index) {
        return extract(pkTrick, CARD_SIZE * index, CARD_SIZE);
    }

    /**
     * @param pkTrick a packed trick
     * @param pkCard a packed card
     * @return the packed trick with that card added
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        return pkTrick & (pkCard << (CARD_SIZE * (size(pkTrick))) |
                ~Bits32.mask(size(pkTrick)* CARD_SIZE, CARD_SIZE));
    }

    /**
     * @param pkTrick a packed trick
     * @return the base color of that trick
     */
    public static Color baseColor(int pkTrick) {
        return PackedCard.color(extract(pkTrick, CARD_0_INDEX, CARD_SIZE));
    }




    /**
     * @param pkTrick a packed trick
     * @param pkHand the hand of a player
     * @return the subset of pkHand that contains the playable cards for the player
     */
    public static long playableCards(int pkTrick, long pkHand) {

        List<Integer> TrumpCards = new ArrayList<>(); //stores the trump cards already played

        boolean wasThereAlreadyATrump = false;

        for (int i = 0; i < size(pkTrick); ++i)
            if (PackedCard.color(card(pkTrick, i)) == trump(pkTrick)) {
                wasThereAlreadyATrump = true;
                TrumpCards.add(card(pkTrick, i));
            }

        TrumpCards.sort(Comparator.comparingInt(i -> PackedCard.rank(i).trumpOrdinal()));
            //sorts the list so that the best trump is at the last position

        int maxTrumpCardPlayed = TrumpCards.size() == 0 ? 0 : TrumpCards.get(TrumpCards.size()-1);
            //maxTrumpCardPlayed is 0 if there was no trump card played, or is the best trump card if there was

        long AllBaseColorCards = subsetOfColor(ALL_CARDS, baseColor(pkTrick));
        long AllTrumpColorCards = subsetOfColor(ALL_CARDS, trump(pkTrick));

        /***************************************************/

        if(size(pkTrick) == 0 || index(pkTrick) == 8)
            return pkHand;

        if(wasThereAlreadyATrump && (subsetOfColor(pkHand, trump(pkTrick)) == pkHand) && (pkHand < singleton(maxTrumpCardPlayed)))
            return pkHand;
        //cases where the player is stuck so he plays whatever he wants

        if((subsetOfColor(pkHand, baseColor(pkTrick)) == 0L)) {

            if(wasThereAlreadyATrump) {

                if (PackedCardSet.difference(pkHand, subsetOfColor(~PackedCardSet.trumpAbove(maxTrumpCardPlayed), trump(pkTrick))) == 0)
                    return pkHand; // plays what he wants when he has no base cards and no higher trump than the previous ones

                return PackedCardSet.difference(pkHand, subsetOfColor(~PackedCardSet.trumpAbove(maxTrumpCardPlayed), trump(pkTrick)));
            }

            return pkHand; //plays what he wants when he has no cards of the base color and no trump was played before
        }


        if(baseColor(pkTrick) == trump(pkTrick)) {

            if(subsetOfColor(pkHand, trump(pkTrick)) ==
                    singleton(PackedCard.pack(trump(pkTrick), Rank.JACK)))

                return pkHand; //plays what he wants when he only has the jack of trump as a trump card

            if((pkHand & AllBaseColorCards) == 0)

                return pkHand; //has no cards that logically follow the previous ones

            return pkHand & AllBaseColorCards;

        } else {

            if(!wasThereAlreadyATrump) {

                if(((AllBaseColorCards | AllTrumpColorCards) & pkHand) == 0)

                    return pkHand;

                return (AllBaseColorCards | AllTrumpColorCards) & pkHand;}

            if (((AllBaseColorCards | PackedCardSet.trumpAbove(maxTrumpCardPlayed)) & pkHand) == 0)

                return pkHand;

            return (AllBaseColorCards | PackedCardSet.trumpAbove(maxTrumpCardPlayed)) & pkHand;
        }
    }



    /**
     * @param pkTrick a packed trick
     * @return the number of points that trick is worth
     */
    public static int points(int pkTrick) {
        int sumOfPoints = 0;

        for(int i = 0; i < size(pkTrick); ++i) {
            sumOfPoints += PackedCard.points(trump(pkTrick), card(pkTrick, i));
        }

        if(isLast(pkTrick))
            sumOfPoints += Jass.LAST_TRICK_ADDITIONAL_POINTS;

        return sumOfPoints;
    }

    /**
     * @param pkTrick a packed trick
     * @return the player that has won the trick
     */
    public static PlayerId winningPlayer(int pkTrick) {
        short relativeWinner = 0;

        for(short i = 1; i < size(pkTrick); ++i)
            if(PackedCard.isBetter(trump(pkTrick), card(pkTrick, i), card(pkTrick, relativeWinner)))
                relativeWinner = i;

        return player(pkTrick, relativeWinner);
    }

    public static String toString(int pkTrick) {
        StringJoiner j = new StringJoiner(",","{","}");

        IntStream.range(0, size(pkTrick)).mapToObj(i -> PackedCard.toString(card(pkTrick, i))).forEachOrdered(j::add);
        //a stringJoiner one-liner

        return j.toString();
    }

}
