package ch.epfl.javass.net;

import ch.epfl.javass.jass.PrintingPlayer;
import ch.epfl.javass.jass.RandomPlayer;

class RemotePlayerServerTest
{
    public static void main(String[] arguments){
        RemotePlayerServer rs = new RemotePlayerServer(new PrintingPlayer(new RandomPlayer(65)));
        rs.run();
    }


}