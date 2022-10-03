package org.qubee.data;

public class StartMessage extends Message{
  private final String opponent;

  private final  Integer timeout;

  private final  GameType game;

  public StartMessage(String opponent, Integer timeout, GameType game) {
    this.opponent = opponent;
    this.timeout = timeout;
    this.game = game;
  }

  public String getOpponent() {
    return opponent;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public GameType getGame() {
    return game;
  }
}
