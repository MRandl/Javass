package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

public final class PrintingPlayer implements Player {

    // /!\ Printing player only displays and transmits the data coming and going from underlyingPlayer
    private final Player underlyingPlayer;
    int tour;

    public PrintingPlayer(Player underlyingPlayer) {
      this.underlyingPlayer = underlyingPlayer;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
      System.out.print("C'est à moi de jouer... Je joue : ");
      Card c = underlyingPlayer.cardToPlay(state, hand);
      System.out.println(c);
      return c;
    }
    
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        System.out.println("Les joueurs sont : ");
        for(PlayerId play : PlayerId.ALL) {
            System.out.println(playerNames.get(play));
        }
        underlyingPlayer.setPlayers(ownId, playerNames);
        tour = 0;
    }
    
    @Override
    public void updateHand(CardSet newHand) {
        System.out.println("J'ai la main suivante : " + newHand);
        underlyingPlayer.updateHand(newHand);
    }
    
    @Override
    public void setTrump(Color trump) {
        System.out.println("J'ai l'atout suivant : " + trump);
        underlyingPlayer.setTrump(trump);
    }
    
    @Override 
    public void updateTrick(Trick newTrick) {
        System.out.println("Le pli " + newTrick.index() + ", commencé par "+ newTrick.player(0) + " : " + newTrick);
        underlyingPlayer.updateTrick(newTrick);
    }
    
    @Override
    public void updateScore(Score score) {
        System.out.println("Le score vaut : " + score);
        underlyingPlayer.updateScore(score);
    }
    
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        System.out.println("L'équipe " + winningTeam + " a gagné !");
        underlyingPlayer.setWinningTeam(winningTeam);
    }
  }