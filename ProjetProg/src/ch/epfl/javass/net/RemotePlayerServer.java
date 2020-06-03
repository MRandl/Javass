package ch.epfl.javass.net;

import ch.epfl.javass.jass.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


import static ch.epfl.javass.net.StringSerializer.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

public final class RemotePlayerServer {
    private Player p;
    private boolean mustContinue = true;


    public RemotePlayerServer(Player p){
        this.p = p;
    }

    /**
     * waits for commands on port 5108 until WINR is received
     */
    public void run(){
        try (ServerSocket s0 = new ServerSocket(5108);
             Socket s = s0.accept(); //waits for a connection
             BufferedReader r =
                     new BufferedReader(
                             new InputStreamReader(s.getInputStream(),
                                     US_ASCII)); //input from jassgame
             BufferedWriter w =
                     new BufferedWriter(
                             new OutputStreamWriter(s.getOutputStream(),
                                     US_ASCII))) {//output to jassgame
             while(mustContinue){
                 String string = r.readLine();
                 switch(JassCommand.valueOf(string.substring(0,4))){
                     case PLRS: //if input received is PLRS, call setPlayers
                         Map<PlayerId, String> m = new HashMap<>();
                         for(int i = 0; i < Jass.NUMBER_OF_PLAYERS; ++i)
                             m.put(PlayerId.ALL.get(i), deserializeString(theSplitter(',',string.substring(7))[i]));
                         //splits the inputs and deserialize them into names

                         PlayerId player = PlayerId.ALL.get(string.charAt(5) - '0') ;
                         p.setPlayers(player,m);
                         break;
                     case HAND: //updates the hand
                         p.updateHand(CardSet.ofPacked(deserializeLong(string.substring(5))));
                         break;
                     case TRMP: //updates the trump
                         p.setTrump(Card.Color.ALL.get(string.charAt(5)-'0'));
                         break;
                     case SCOR: //updates the score
                         p.updateScore(Score.ofPacked(deserializeLong(string.substring(5))));
                         break;
                     case TRCK: //updates the trick
                         p.updateTrick(Trick.ofPacked(deserializeInt(string.substring(5))));
                         break;
                     case CARD: //asks for the player to play a card
                         String[] commandString = theSplitter(' ', string); //the arguments that came with CARD
                         String[] serializedTurnstateArray = theSplitter(',', commandString[1]); //splits the turnstate
                         TurnState ts = TurnState.ofPackedComponents(deserializeLong(serializedTurnstateArray[0]),
                                                                     deserializeLong(serializedTurnstateArray[1]),
                                                                     deserializeInt(serializedTurnstateArray[2])); //recreates the turnstate

                         Card c = p.cardToPlay(ts, CardSet.ofPacked(deserializeLong(commandString[2]))); //card to play
                         w.write(serializeInt(c.packed()));
                         w.write("\n");
                         w.flush();//send it back to jassgame
                         break;
                     case WINR: //calls setWinners and stops
                         p.setWinningTeam(TeamId.ALL.get(string.charAt(5)-'0'));
                         mustContinue = false;
                         break;
                 }
             }


        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }
}
