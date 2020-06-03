package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class GraphicalPlayer {

    private static final ObservableMap<Card, Image> cardToImage = generateCardToImage();
        // maps all possible jass cards to their respective image
    private static final ObservableMap<Card.Color, Image> trumpToImage = generateTrumpToImage();
        // maps all possible jass colors to their respective image
    private Scene scene;
        // stored here because it is used across two different methods


    /**
     * @param p the playerId of the current instance of player
     * @param nameMap maps playerIds to their respective name in the current game
     * @param sB the scorebean that stores all the score data about the game
     * @param tB the trickbean that stores all the trick data about the game
     * @param hB the handbean that stores all the hand data about the game
     * @param abq an arrayBlockingQueue that will be used as a bridge between the interface and the model
     * @param scale the window scale, bigger value recommended for bigger/more pixel-dense screens.
     *              Introduced because the different computers used for development have very different resolutions
     */
    public GraphicalPlayer(PlayerId p, Map<PlayerId, String> nameMap, ScoreBean sB, TrickBean tB, HandBean hB, ArrayBlockingQueue<Card> abq, double scale){
        VBox v = new VBox();
        v.getChildren().addAll(createScorePane(sB, nameMap, scale), createTrickPane(p, nameMap, tB, scale), createHandPane(hB, abq, scale));
        StackPane pane = new StackPane(v, createWinningPane(sB, TeamId.TEAM_1, scale), createWinningPane(sB, TeamId.TEAM_2, scale));
        this.scene = new Scene(pane);
    }

    /**
     * @return the stage of the represented player
     */
    public Stage createStage(){
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Javass");
        stage.setResizable(false);
        return stage;
    }

    /**
     * @return an observable map that maps all possible cards to their images
     */
    private static ObservableMap<Card, Image> generateCardToImage() {
        ObservableMap<Card, Image> finalMap = FXCollections.observableHashMap();
        for(short i = 0; i < 4; ++i)
            for(short j = 0; j < 9; ++j)
                finalMap.put(Card.of(Card.Color.ALL.get(i), Card.Rank.ALL.get(j)),
                        new Image("/card_"+i+"_"+j+"_240.png"));
        return finalMap;
    }

    /**
     * @return an observable map that maps all possible colors to their images
     */
    private static ObservableMap<Card.Color, Image> generateTrumpToImage() {
        ObservableMap<Card.Color, Image> finalMap = FXCollections.observableHashMap();
        for(short i = 0; i < 4; ++i)
            finalMap.put(Card.Color.ALL.get(i), new Image("/trump_" + i + ".png"));
        return finalMap;
    }

    /**
     * @param sB the scorebean the player should use
     * @param map the map from playerIds to names
     * @param scale the scale of the pane. See javadoc of constructor
     * @return the pane containing the score info
     */
    private static GridPane createScorePane(ScoreBean sB, Map<PlayerId, String> map, double scale){

        GridPane grid = new GridPane();                       //will be returned
        grid.setStyle("-fx-font: " + (int)(10 * scale) +
                " Optima; -fx-background-color: #d3d3d3;" +
                " fx-padding: 5px; -fx-alignment: center;");

        for(TeamId team : TeamId.ALL) {

            grid.add(new Label(map.get(PlayerId.ALL.get(team.ordinal())) + " et "
                + map.get(PlayerId.ALL.get(team.ordinal() + 2)) + " :"), 0, team.ordinal());
            //adds the player names in the grid

            Text turnPoints = new Text();
            turnPoints.textProperty().bind(Bindings.convert(sB.turnPointsProperty(team)));
            grid.add(turnPoints, 1, team.ordinal());
            //adds the turn points in the grid

            Text trickPoints = new Text("(+0) ");
            sB.turnPointsProperty(team).addListener(
                    (o, oV, nV) -> {
                        if(nV.intValue() - oV.intValue() >= 10)
                            trickPoints.setText("(+" + (nV.intValue() - oV.intValue()) + ")");
                        else if(nV.intValue() - oV.intValue() > 0)
                            trickPoints.setText("(+" + (nV.intValue() - oV.intValue()) + ") ");
                            //note the space afterwards, if the new value is between 0 and 9 a space pops
                            //up so that the rest of the grid (to the right) doesn't move
                        else
                            trickPoints.setText("(+0) "); // so that between tricks, the reset of turnPoints doesnt print negative values
                    }); //listens and doesn't bind so that we can have a precise control over the spaces, the values printed, etc...
            grid.add(trickPoints, 2, team.ordinal());
            //adds the trick points in the grid

            grid.add(new Label("/Total :"), 3, team.ordinal());
            // adds a static text in the grid

            Text gamePoints = new Text();
            gamePoints.textProperty().bind(Bindings.convert(sB.gamePointsProperty(team)));
            grid.add(gamePoints, 4, team.ordinal());
            //adds the total points to the grid
        }
        return grid;
    }

    /**
     * @param me the playerId of the current instance of player
     * @param nameMap maps playerIds to their respective name in the game
     * @param tb the current trickBean of the player
     * @param scale the scale of the window. See javadoc of constructor
     * @return the pane representing a trick (cards played, current trump etc)
     */
    private static GridPane createTrickPane(PlayerId me,
                            Map<PlayerId, String> nameMap, TrickBean tb, double scale){

        GridPane grid = new GridPane();
            //will be returned
        grid.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px;" +
                " -fx-border-width: 3px 0px; -fx-border-style: solid;" +
                " -fx-border-color: gray; -fx-alignment: center;");

        short[] xCoordinates = new short[]{1,2,1,0};
                          // replace with {1,0,1,2} to display the cards clockwise instead of counterclockwise

        short[] yCoordinates = new short[]{2,0,0,0};
        short[] cellYSize =    new short[]{1,3,1,3};
        //the arrays describe where the different cards will be placed (xCoordinates and yCoordinates)
        //and their respective vertical size (cellYSize) in number of cells. Note that card #0 is the current player's one,
        //therefore the centered-bottom one, card #1 is the next player's, at the right of the screen, etc etc

        for(PlayerId cardPlayer : PlayerId.ALL) {

            VBox v = new VBox(); //will contain the cards played and the name of the player that played the card in question
            v.setAlignment(Pos.CENTER);

            Text playerName = new Text(); //the name
            playerName.setText(nameMap.get(cardPlayer));
            playerName.setFont(Font.font("Optima", (int)(10 * scale)));
            //no need for a property as the name/font of a player does not evolve with time

            StackPane cardAndRectangle = new StackPane(); //cards + red rectangle
            Rectangle rectangle = new Rectangle();
            rectangle.setHeight(90 * scale);
            rectangle.setWidth(60*scale);
            rectangle.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent; " +
                    "-fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
            rectangle.setEffect(new GaussianBlur(4));
            rectangle.visibleProperty().bind(tb.getWinningPlayer().isEqualTo(cardPlayer));

            ImageView iv = new ImageView(); // the actual image of the card
            iv.imageProperty().bind(Bindings.valueAt(cardToImage, Bindings.valueAt(tb.getTrick(), cardPlayer)));
            iv.setFitHeight(90 * scale);
            iv.setFitWidth(60 * scale);

            cardAndRectangle.getChildren().addAll(rectangle, iv); //cardAndRectangle contains the card and the red rectangle

            if(cardPlayer == me) { //if the card corresponds to the player of the adapter, the text should be at the bottom
                v.getChildren().addAll(cardAndRectangle, playerName);
            } else {
                v.getChildren().addAll(playerName, cardAndRectangle);
            }

            short relativePlayerOrdinal = (short) ((cardPlayer.ordinal() + (4 - me.ordinal()))%4);
            //+(4 - x) is the same as -x when mod 4, but 4-x is positive
            // and thus makes sure the %4 operator behaves as planned.
            //relativePlayerOrdinal give the position of cardPlayer if me is 0

            grid.add(v,
                    xCoordinates[relativePlayerOrdinal],
                    yCoordinates[relativePlayerOrdinal],
                    1,
                    cellYSize[relativePlayerOrdinal]
                    ); //the final grid contains the cards placed on the bottom, left, up, right of the grid
        }

        //trump display :
        ImageView iv = new ImageView();
        iv.setFitHeight(50*scale);
        iv.setFitWidth(50*scale);
        iv.imageProperty().bind(Bindings.valueAt(trumpToImage, tb.getTrump()));
        grid.add(iv, 1, 1);
        GridPane.setHalignment(iv, HPos.CENTER);


        return grid;
    }

    /**
     * @param hB the handBean the current instance of player uses
     * @param abq an arrayBlockingQueue used for one-way communication from GPlayer to GPlayerAdapter
     * @param scale the scale of the window (see constructor)
     * @return the pane that displays and enables interactio with the hand of the player
     */
    private static Pane createHandPane(HandBean hB, ArrayBlockingQueue<Card> abq, double scale){
        HBox hbox = new HBox(); //will be returned
        hbox.setStyle("-fx-background-color: lightgray; -fx-spacing: 5px;-fx-padding: 5px;");
        for(int i = 0; i < Jass.HAND_SIZE; ++i ){
            int finalI = i; //used because bindings don't like non-final values such as a for iterator

            SimpleBooleanProperty isPlayable = new SimpleBooleanProperty();
            isPlayable.bind(Bindings.createBooleanBinding(() -> hB.playableCards().contains(hB.hand().get(finalI)), hB.playableCards(), hB.hand()));

            ImageView iv = new ImageView();
            iv.imageProperty().bind(Bindings.valueAt(cardToImage, Bindings.valueAt(hB.hand(), finalI)));
            iv.setFitWidth(40 * scale);
            iv.setFitHeight(60 * scale);
            iv.opacityProperty().bind(new When(isPlayable).then(1).otherwise(.2));
            iv.disableProperty().bind(isPlayable.not());
            iv.setOnMouseClicked(event ->  abq.offer(hB.hand().get(finalI)));

            hbox.getChildren().add(iv);
        }
        return hbox;
    }

    /**
     * @param sB the scoreBean the current player uses
     * @param t the team of the created winning pane
     * @param scale the scale of the window (see constructor)
     * @return
     */
    private static Pane createWinningPane(ScoreBean sB, TeamId t, double scale){
        Text victoryText = new Text();

        victoryText.textProperty().bind(Bindings.format("L'équipe " + t + " a gagné avec %d points contre %d" ,
                sB.totalPointsProperty(t), sB.totalPointsProperty(t.other())));

        BorderPane pane = new BorderPane(victoryText);
        pane.setStyle(String.format("-fx-font: %d Optima; -fx-background-color: whitesmoke;", (int)(16 * scale)));
        pane.visibleProperty().bind(sB.winningTeamProperty().isEqualTo(t));
        return pane;
    }
}
