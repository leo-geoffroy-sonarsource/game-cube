package org.qubee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qubee.data.GameType;

public class RPSQubeGame implements QubeGame {

  public static final int GAME_TIMEOUT = 5;

  private List<String> opponents = new ArrayList<>();


  public RPSQubeGame() {
  }

  public void addOpponents(String username) {
    opponents.add(username);
  }

  @Override
  public Integer timeout() {
    return GAME_TIMEOUT;
  }

  public List<String> getOpponents() {

    return Collections.unmodifiableList(opponents);
  }

  @Override
  public void addOpponent(String username) {
    opponents.add(username);
  }

  @Override
  public GameType getGameType() {
    return GameType.ROCKPAPERSCISSORS;
  }
}
