package ch.epfl.javass.jass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

public class RemoteMain extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread remoteThread = new Thread(() -> {
            RemotePlayerServer rs = new RemotePlayerServer(new GraphicalPlayerAdapter());
            System.out.println("La partie commencera à la connexion du serveur !");
            rs.run();
        });
        remoteThread.setDaemon(true);
        remoteThread.start();
    }
}
