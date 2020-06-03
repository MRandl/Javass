package ch.epfl.javass.jass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerClient;

import java.util.HashMap;
import java.util.Map;
//no comments, this is not an official class
public final class RandomJassGame {
    public static void main(String[] args) {
        Map<PlayerId, Player> players = new HashMap<>();
        Map<PlayerId, String> playerNames = new HashMap<>();

        for (PlayerId pId: PlayerId.ALL) {
            Player player = new RandomPlayer(2017);
            if (pId == PlayerId.PLAYER_1)
                player = new GraphicalPlayerAdapter();

            players.put(pId, player);
            playerNames.put(pId, pId.name());
        }

        JassGame g = new JassGame(2017, players, playerNames);
        while (! g.isGameOver()) {
            g.advanceToEndOfNextTrick();
            System.out.println("----");
        }
    }
}