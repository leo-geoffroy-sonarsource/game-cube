package org.qubee;

import java.util.List;
import org.qubee.data.GameType;

public interface QubeGame{

  Integer timeout();

  List<String> getOpponents();

  public void addOpponent(String username);

  GameType getGameType();
}
