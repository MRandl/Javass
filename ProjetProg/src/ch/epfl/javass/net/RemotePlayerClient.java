package ch.epfl.javass.net;

import ch.epfl.javass.jass.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import static ch.epfl.javass.net.StringSerializer.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

public final class RemotePlayerClient implements Player, AutoCloseable {

    private BufferedReader r;
    private BufferedWriter w;
    private Socket s;

    /**
     * @param name the IP address of the remote player
     * @throws IOException when either the IP address is not found or when it is not responding
     */
    public RemotePlayerClient(String name) throws IOException{
        s = new Socket(name, 5108);
             r = new BufferedReader(
                             new InputStreamReader(s.getInputStream(),
                                     US_ASCII)); //reads the inputs from the remote player
             w = new BufferedWriter(
                             new OutputStreamWriter(s.getOutputStream(),
                                     US_ASCII)); //sends data to the remote player
    }

    /**
     * @param state the current turnState of the game
     * @param hand the hand of the player
     * @return the card the remote player has decided to play
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        try {
            String toBeSent = "CARD " + theJoiner(',', serializeLong(state.packedScore()),
                    serializeLong(state.packedUnplayedCards()),
                    serializeInt(state.packedTrick()))
                    + " " + serializeLong(hand.packed()); //sends "CARD" plus info about the arguments
            w.write(toBeSent);
            w.write('\n');
            w.flush();//sends it
            String s = r.readLine();//wait for the answer
            return Card.ofPacked(deserializeInt(s));
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param ownId the playerId of the current player
     * @param playerNames map playerIds to the respective players names
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        try{
            String[] playerPlainText = new String[4];
            for(int i = 0; i < 4; ++i)
                playerPlainText[i] = serializeString(playerNames.get(PlayerId.ALL.get(i))); //store all names
            String toBeSent = "PLRS " + ownId.ordinal() + " " + theJoiner(',', playerPlainText);
            //toBeSent contains PLRS + the info about the current player + the other players' names
            w.write(toBeSent);
            w.write('\n');
            w.flush();//send all of it
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param newHand the updated hand of the player
     */
    @Override
    public void updateHand(CardSet newHand) {
        try{
            w.write("HAND " + StringSerializer.serializeLong(newHand.packed()));
            w.write('\n');
            w.flush();//send the packed version
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param trump the updated trump
     */
    @Override
    public void setTrump(Card.Color trump) {
        try{
            w.write("TRMP " + trump.ordinal());
            w.write('\n');
            w.flush(); //sends the ordinal of the new trump
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param newTrick the updated trick
     */
    @Override
    public void updateTrick(Trick newTrick) {
        try{
            w.write("TRCK " + StringSerializer.serializeInt(newTrick.packed()));
            w.write('\n');
            w.flush(); //sends the packed version of the new trick
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param score the updated score
     */
    @Override
    public void updateScore(Score score) {
        try{
            w.write("SCOR " + StringSerializer.serializeLong(score.packed()));
            w.write('\n');
            w.flush(); //sends the packed version of the new score
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param winningTeam the winning team
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        try{
            w.write("WINR " + winningTeam.ordinal());
            w.write('\n');
            w.flush(); //sends the ordinal of the winning team
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * required by AutoCloseable
     * @throws Exception if an I/O mistake occurred while closing the streams
     */
    @Override
    public void close() throws Exception {
        w.close();
        r.close();
        s.close();
    }
}
