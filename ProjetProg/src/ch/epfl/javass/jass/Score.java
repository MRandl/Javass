package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * @author Mathis Randl
 *
 */
public final class Score {

    private Score(long pkdScore) {
        checkArgument(PackedScore.isValid(pkdScore));
        this.pkdScore = pkdScore;
    }

    private long pkdScore;

    public static final Score INITIAL = new Score(0);

    /**
     * @param packed
     *            a packed score
     * @return the corresponding "unpacked" score
     * @throws IllegalArgumentException if packed is invalid
     */
    public static Score ofPacked(long packed) {
        return new Score(packed);
    }

    // a getter
    public long packed() {
        return pkdScore;
    }

    /**
     * @param t
     *            a team
     * @return the tricks won in that turn by that team
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(pkdScore, t);
    }

    /**
     * @param t
     *            a team
     * @return the points won in that turn by that team
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(pkdScore, t);
    }

    /**
     * @param t
     *            a team
     * @return the points won previously by that team
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(pkdScore, t);
    }

    /**
     * @param t
     *            a team
     * @return the total points the team has earned during the game
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(pkdScore, t);
    }

    /**
     * @param winningTeam
     *            a team
     * @param trickPoints
     *            a number of points to be added
     * @return the updated score that takes into account the fact the the team
     *         has won a trick and thus trickpoints points
     * @throws IllegalArgumentException if trickPoints is negative
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
        checkArgument(trickPoints >= 0);
        return ofPacked(PackedScore.withAdditionalTrick(pkdScore, winningTeam,
                trickPoints));
    }

    /**
     * @return the updated score at the end of a turn
     */
    public Score nextTurn() {
        return ofPacked(PackedScore.nextTurn(pkdScore));
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Score) && this.pkdScore == ((Score) o).packed();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pkdScore);
    }

    @Override
    public String toString() {
        return PackedScore.toString(this.pkdScore);
    }

}
