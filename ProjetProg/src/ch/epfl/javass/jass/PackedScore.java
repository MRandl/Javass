package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits64.*;

/**
 * @author Mathis Randl
 *
 */
public final class PackedScore {
    private PackedScore() {
    }

    public static final long INITIAL = 0;

    private final static int WON_TRICKS_POS = 0;
    private final static int WON_TRICKS_SIZE = 4;
    private final static int TURN_POINTS_POS = 4;
    private final static int TURN_POINTS_SIZE = 9;
    private final static int GAME_POINTS_POS = 13;
    private final static int GAME_POINTS_SIZE = 11;
    private final static int ZERO_POS = 24;
    private final static int ZERO_SIZE = 8;

    /**
     * @param pkScore
     *            a packed score
     * @return true if the packed score only contains legal values, false otherwise
     */
    public static boolean isValid(long pkScore) {
        boolean assertion = true;

        assertion = assertion && (extract(pkScore, WON_TRICKS_POS, WON_TRICKS_SIZE) <= 9)
                && (extract(pkScore, TURN_POINTS_POS, TURN_POINTS_SIZE) <= 257)
                && (extract(pkScore, GAME_POINTS_POS, GAME_POINTS_SIZE) <= 2000)
                && (extract(pkScore, ZERO_POS, ZERO_SIZE) == 0);

        pkScore >>>= 32;

        assertion = assertion && (extract(pkScore, WON_TRICKS_POS, WON_TRICKS_SIZE) <= 9)
                && (extract(pkScore, TURN_POINTS_POS, TURN_POINTS_SIZE) <= 257)
                && (extract(pkScore, GAME_POINTS_POS, GAME_POINTS_SIZE) <= 2000)
                && (extract(pkScore, ZERO_POS, ZERO_SIZE) == 0);

        return assertion;
    }

    /**
     * @param turnTricks1
     *            the number of tricks won by team 1
     * @param turnPoints1
     *            the number of points won by team 1 during the turn
     * @param gamePoints1
     *            the number of points won by team 1 before the turn
     * @param turnTricks2
     *            the number of tricks won by team 2
     * @param turnPoints2
     *            the number of points won by team 2 during the turn
     * @param gamePoints2
     *            the number of points won by team 2 before the turn
     * @return the score that packs all of them
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1,
                            int turnTricks2, int turnPoints2, int gamePoints2) {

        int[] values = new int[] { turnTricks1, turnPoints1, gamePoints1,
                                   turnTricks2, turnPoints2, gamePoints2 }; //store them in an array

        int[] bitshift = new int[] { 0, 4, 13, 32, 36, 45 }; //store the bitshift for every value

        long finalResult = 0L;

        for (int i = 0; i <= 5; ++i) {
            finalResult |= ((long) values[i] << bitshift[i]);
        }
        return finalResult;
    }

    /**
     * @param pkScore
     *            a packed score
     * @param t
     *            a team
     * @return the number of tricks won by that team, according to that score
     */
    public static int turnTricks(long pkScore, TeamId t) {
        return (int) extract(pkScore, (t == TeamId.TEAM_1 ? WON_TRICKS_POS : 32 + WON_TRICKS_POS), WON_TRICKS_SIZE);
        //extracts the searched number at position 0 or 32, depending on t
    }

    /**
     * @param pkScore
     *            a packed score
     * @param t
     *            a team
     * @return the number of points won by that team in the current turn,
     *         according to that score
     */
    public static int turnPoints(long pkScore, TeamId t) {
        return (int) extract(pkScore, t == TeamId.TEAM_1 ? TURN_POINTS_POS : (32 + TURN_POINTS_POS), TURN_POINTS_SIZE);
    }

    /**
     * @param pkScore
     *            a packed score
     * @param t
     *            a team
     * @return the number of points won by that team in the previous turns,
     *         according to that score
     */
    public static int gamePoints(long pkScore, TeamId t) {
        return (int) extract(pkScore, t == TeamId.TEAM_1 ? GAME_POINTS_POS : 32 + GAME_POINTS_POS, GAME_POINTS_SIZE);
    }

    /**
     * @param pkScore
     *            a packed score
     * @param t
     *            a team
     * @return the total number of points earned by that team during the whole
     *         game
     */
    public static int totalPoints(long pkScore, TeamId t) {
        return gamePoints(pkScore, t) + turnPoints(pkScore, t);
    }

    /**
     * @param pkScore
     *            a packed score
     * @param winningTeam
     *            a team
     * @param trickPoints
     *            a number of points
     * @return the updated packed score
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam,
                                           int trickPoints) {

        pkScore += ((winningTeam == TeamId.TEAM_1) ? 1 :  1L << 32);
        // adds a won trick to the winning team

        if (turnTricks(pkScore, winningTeam) == 9)
            trickPoints += Jass.MATCH_ADDITIONAL_POINTS;
        // adds 100 points for an all win situation

        pkScore += (winningTeam == TeamId.TEAM_1 ?
                trickPoints << TURN_POINTS_POS: (long) trickPoints << (32 + TURN_POINTS_POS));
        // adds the trick points

        return pkScore;
    }

    /**
     * @param pkScore
     *            a packed score
     * @return a packed score that is empty except for the total points won by
     *         the teams
     */
    public static long nextTurn(long pkScore) {
        return pack(0, 0, totalPoints(pkScore, TeamId.TEAM_1),
                    0, 0, totalPoints(pkScore, TeamId.TEAM_2));
    }

    public static String toString(long pkScore) {
        return "(" + turnTricks(pkScore, TeamId.TEAM_1) + ","
                + turnPoints(pkScore, TeamId.TEAM_1) + ","
                + gamePoints(pkScore, TeamId.TEAM_1) + ")/("
                + turnTricks(pkScore, TeamId.TEAM_2) + ","
                + turnPoints(pkScore, TeamId.TEAM_2) + ","
                + gamePoints(pkScore, TeamId.TEAM_2) + ")";
    }
}
