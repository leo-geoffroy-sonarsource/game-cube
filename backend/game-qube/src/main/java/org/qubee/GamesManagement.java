package org.qubee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GamesManagement {

  private final Map<String, Integer> scores = new HashMap<>();

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

  public void incrementScore(String username) {
    scores.putIfAbsent(username, 0);
    scores.put(username, scores.get(username) + 1);
  }

  public Map<String, Integer> getScores() {
    return scores;
  }

  public void clearScore(){
    scores.clear();
  }
}
