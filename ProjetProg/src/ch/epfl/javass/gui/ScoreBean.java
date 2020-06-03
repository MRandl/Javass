package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.*;


public final class ScoreBean {
    private SimpleIntegerProperty
            turnPoints1 = new SimpleIntegerProperty(),
            gamePoints1 = new SimpleIntegerProperty(),
            totalPoints1 = new SimpleIntegerProperty(),
            turnPoints2 = new SimpleIntegerProperty(),
            gamePoints2 = new SimpleIntegerProperty(),
            totalPoints2 = new SimpleIntegerProperty();
    private SimpleObjectProperty<TeamId> winningTeam = new SimpleObjectProperty<>();

    /**
     * @param t a team
     * @return the turn points of the team t
     */
    ReadOnlyIntegerProperty turnPointsProperty(TeamId t){
        return t == TeamId.TEAM_1 ? turnPoints1 : turnPoints2;
    }

    /**
     * @param t a team
     * @param newTurnPoints the new turn points
     */
    void setTurnPoints(TeamId t, int newTurnPoints){
        if (t == TeamId.TEAM_1) turnPoints1.set(newTurnPoints);
        else turnPoints2.set(newTurnPoints);
    }

    /**
     * @param t a team
     * @return the game points of the team t
     */
    ReadOnlyIntegerProperty gamePointsProperty(TeamId t){
        return t == TeamId.TEAM_1 ? gamePoints1 : gamePoints2;
    }

    /**
     * @param t a team
     * @param newGamePoints the new game points of the players
     */
    void setGamePoints(TeamId t, int newGamePoints){
        if (t == TeamId.TEAM_1) gamePoints1.set(newGamePoints);
        else gamePoints2.set(newGamePoints);
    }

    /**
     * @param t a team
     * @return the total points of the team t
     */
    ReadOnlyIntegerProperty totalPointsProperty(TeamId t){
        return t == TeamId.TEAM_1 ? totalPoints1 : totalPoints2;
    }

    /**
     * @param t a team
     * @param newTotalPoints the new total points of the player
     */
    void setTotalPoints(TeamId t, int newTotalPoints){
        if (t == TeamId.TEAM_1) totalPoints1.set(newTotalPoints);
        else totalPoints2.set(newTotalPoints);
    }

    /**
     * @return the winning team
     */
    ReadOnlyObjectProperty<TeamId> winningTeamProperty(){
        return winningTeam;
    }

    /**
     * @param t the team that won
     */
    void setWinningTeam(TeamId t){
        winningTeam.set(t);
    }
}
