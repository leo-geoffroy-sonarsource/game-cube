package org.qubee.data.message;

import java.util.ArrayList;
import java.util.List;

public class LobbyMessage extends Message {
  List<PlayerWithScore> players = new ArrayList<>();

  public LobbyMessage() {
  }

  public List<PlayerWithScore> getPlayers() {
    return players;
  }

  public void setPlayers(List<PlayerWithScore> players) {
    this.players = players;
  }

  public void addPlayer(String username, Integer score){
    PlayerWithScore playerWithScore = new PlayerWithScore();
    playerWithScore.setScore(score);
    playerWithScore.setUsername(username);
    players.add(playerWithScore);
  }
}
