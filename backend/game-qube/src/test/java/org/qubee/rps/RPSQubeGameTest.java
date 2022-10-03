package org.qubee.rps;

import org.junit.jupiter.api.Test;

public class RPSQubeGameTest {

  @Test
  public void test(){
    RPSQubeGame rpsQubeGame = new RPSQubeGame();
    rpsQubeGame.addOpponent("test1");
    rpsQubeGame.addOpponent("test2");

    rpsQubeGame.registerAction("test1", RpsActionType.ROCK);
    rpsQubeGame.registerAction("test1", RpsActionType.PAPER);
  }
}
