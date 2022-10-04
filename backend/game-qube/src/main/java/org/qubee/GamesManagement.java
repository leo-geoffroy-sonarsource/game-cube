package org.qubee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.websocket.Session;

public class GamesManagement {

  List<QubeGame> games = new ArrayList<>();


  public void addGame(QubeGame game) {
    games.add(game);
  }

  public void removeGame(QubeGame game) {
    games.remove(game);
  }


  public QubeGame findGame(String username) {
    return findGameByParticipant(username)
      .orElseThrow(() -> {
        throw new IllegalArgumentException("Game does not exist");
      });
  }

  public Optional<QubeGame> findGameByParticipant(String username) {
    return games.stream()
      .filter(g -> g.getParticipants().contains(username))
      .findFirst();
  }
}
