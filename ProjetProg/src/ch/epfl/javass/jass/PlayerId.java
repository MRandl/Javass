package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathis Randl
 *
 */
public enum PlayerId {

    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;
    public final static List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    
    public final static int COUNT = 4;

    /**
     * @return the teamId that the current player belongs to
     */
    public TeamId team() {
        return (this == PLAYER_1 || this == PLAYER_3) ? 
                TeamId.TEAM_1 : TeamId.TEAM_2;
    }

}
