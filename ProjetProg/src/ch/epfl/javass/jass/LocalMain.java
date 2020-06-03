package ch.epfl.javass.jass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class LocalMain extends Application {

    private final static String[] defaultNames = new String[]{"Aline", "Bastien", "Colette", "David"};

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> parameters = new ArrayList<>(getParameters().getRaw());

        if(parameters.size() != 4 && parameters.size() != 5){
            System.err.println("Utilisation: java ch.epfl.javass.LocalMain <j1>…<j4> [<graine>]\n" +
                    "où :\n" +
                    "<jn> spécifie le joueur n, suivant la nomenclature ci-dessous:\n" +
                    "  h:<nom>          un joueur humain nommé <nom>\n" +
                    "  h                un joueur humain nommé par défaut (Aline, Bastien, Colette, David)\n" +
                    "  s:[<nom>]:<ite>  un joueur simulé utilisant <ite> itérations éventuellement nommé <nom>,\n" +
                    "                        sinon nommé par défaut\n" +
                    "  r:[<nom>]:<adr>  un joueur humain à distance, localisé à l'adresse IP <adr>,\n" +
                    "                        éventuellement nommé <nom>, sinon nommé par défaut");
            System.exit(-1);
        }

        Random rngSeed = null;
        long[] rngSeeds = new long[4];
        if(parameters.size()==4){
            rngSeed = new Random();
        } else {
            try {
                rngSeed = new Random(Long.parseLong(parameters.get(4)));
            } catch (NumberFormatException e) {
                System.err.println("RNG seed is not a valid long");
                System.exit(-12);
            }
        }
        long jassGameRandomArgument = rngSeed.nextLong();

        for(int i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i){
            rngSeeds[i] = rngSeed.nextLong();
        }

        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);

        for(int i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i){
            String[] playerArgs = parameters.get(i).split(":");

            switch (playerArgs[0]) {
                case "h":
                    switch (playerArgs.length) {
                        case 1:
                            players.put(PlayerId.ALL.get(i), new GraphicalPlayerAdapter());
                            playerNames.put(PlayerId.ALL.get(i), defaultNames[i]);
                            break;

                        case 2:
                            players.put(PlayerId.ALL.get(i), new GraphicalPlayerAdapter());
                            playerNames.put(PlayerId.ALL.get(i), playerArgs[1]);
                            break;

                        default:
                            System.err.println("Illegal number of arguments (" + playerArgs.length + ") for player " + (i + 1) + ". Expected 1 or 2");
                            System.exit(-3);
                    }
                break;

                case "s":
                    switch (playerArgs.length) {
                        case 1:
                            players.put(PlayerId.ALL.get(i), new PacedPlayer(new MctsPlayer(PlayerId.ALL.get(i), rngSeeds[i], 10000), 1));
                            playerNames.put(PlayerId.ALL.get(i), defaultNames[i]);
                            break;

                        case 2:
                            players.put(PlayerId.ALL.get(i), new PacedPlayer(new MctsPlayer(PlayerId.ALL.get(i), rngSeeds[i], 10000),1));
                            playerNames.put(PlayerId.ALL.get(i), playerArgs[1]);
                            break;

                        case 3:
                            try {
                                if(Integer.parseInt(playerArgs[2]) < 10)
                                    throw new NumberFormatException();
                                players.put(PlayerId.ALL.get(i), new PacedPlayer(new MctsPlayer(PlayerId.ALL.get(i), rngSeeds[i], Integer.parseInt(playerArgs[2])), 1));
                            } catch (NumberFormatException e){
                                System.err.println("Third argument of simulated player " + (i + 1) + "is no number or is smaller than 10.");
                                System.exit(-7);
                            }

                            if(playerArgs[1].equals("")){
                                playerNames.put(PlayerId.ALL.get(i), defaultNames[i]);
                            } else {
                                playerNames.put(PlayerId.ALL.get(i), playerArgs[1]);
                            }
                            break;

                        default:
                            System.err.println("Illegal number of arguments (" + playerArgs.length + ") for player " + (i + 1) + ". Expected 1, 2 or 3.");
                            System.exit(-4);
                    }
                break;

                case "r":
                    switch (playerArgs.length) {
                        case 1 :
                            try {
                                players.put(PlayerId.ALL.get(i), new RemotePlayerClient("localhost"));
                            } catch (IOException e){
                                System.err.println("Could not connect to 'localhost'");
                                System.exit(-9);
                            }
                            playerNames.put(PlayerId.ALL.get(i), defaultNames[i]);
                            break;

                        case 2:
                            try {
                                players.put(PlayerId.ALL.get(i), new RemotePlayerClient("localhost"));
                            } catch (IOException e){
                                System.err.println("Could not connect to 'localhost'");
                                System.exit(-10);
                            }
                            playerNames.put(PlayerId.ALL.get(i), playerArgs[1]);
                            break;

                        case 3:
                            try {
                                players.put(PlayerId.ALL.get(i), new RemotePlayerClient(playerArgs[2]));
                            } catch (IOException e){
                                System.err.println("Could not connect to " + playerArgs[2]);
                                System.exit(-11);
                            }
                            if(playerArgs[1].equals("")){
                                playerNames.put(PlayerId.ALL.get(i), defaultNames[i]);
                            } else {
                                playerNames.put(PlayerId.ALL.get(i), playerArgs[1]);
                            }
                            break;

                        default:
                            System.err.println("Illegal number of arguments (" + playerArgs.length + ") for player " + (i + 1) + ". Expected 1, 2 or 3.");
                            System.exit(-8);
                    }

                break;

                default:
                    System.err.println("Argument number " + (i + 1) + " : first player argument is not 'h', 's' or 'r'.");
                    System.exit(-2);
            }
        }

        Thread gameThread = new Thread(() -> {
            JassGame j = new JassGame(jassGameRandomArgument, players, playerNames);
            while (! j.isGameOver()) {
                j.advanceToEndOfNextTrick();
                try { Thread.sleep(1000); } catch (Exception e) { e.printStackTrace();}
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
}

