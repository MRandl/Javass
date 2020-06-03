package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathis Randl
 *
 */
public enum TeamId {
    TEAM_1, TEAM_2;
    public final static List<TeamId> ALL = 
            Collections.unmodifiableList(Arrays.asList(values()));
    public final static int COUNT = 2;
    
    /**
     * @return the teamId that the current value is not
     */
    public TeamId other() {
        return (this == TEAM_1) ? TEAM_2 : TEAM_1;
    }
}
