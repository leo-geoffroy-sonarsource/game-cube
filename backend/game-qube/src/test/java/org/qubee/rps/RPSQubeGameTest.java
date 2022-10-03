package org.qubee.rps;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubee.ResultType;

public class RPSQubeGameTest {

  @Test
  public void testGameStandard(){
    RPSQubeGame rpsQubeGame = new RPSQubeGame();
    rpsQubeGame.addOpponent("test1");
    rpsQubeGame.addOpponent("test2");

    rpsQubeGame.registerAction("test1", RpsActionType.ROCK);
    rpsQubeGame.registerAction("test2", RpsActionType.PAPER);
    Assertions.assertThat(rpsQubeGame.getWinner()).isEqualTo("test2");
  }


  @Test
  public void testGameTimeout(){
    RPSQubeGame rpsQubeGame = new RPSQubeGame();
    rpsQubeGame.addOpponent("test1");
    rpsQubeGame.addOpponent("test2");

    rpsQubeGame.registerAction("test1", RpsActionType.ROCK);
    rpsQubeGame.registerTimeout("test2");
    Assertions.assertThat(rpsQubeGame.getWinner()).isEqualTo("test1");
  }

  @Test
  public void testGameTimeoutBeforeAction(){
    RPSQubeGame rpsQubeGame = new RPSQubeGame();
    rpsQubeGame.addOpponent("test1");
    rpsQubeGame.addOpponent("test2");

    rpsQubeGame.registerTimeout("test2");
    rpsQubeGame.registerAction("test1", RpsActionType.ROCK);
    Assertions.assertThat(rpsQubeGame.getWinner()).isEqualTo("test1");
  }
  @Test
  public void testGameTie(){
    RPSQubeGame rpsQubeGame = new RPSQubeGame();
    rpsQubeGame.addOpponent("test1");
    rpsQubeGame.addOpponent("test2");

    rpsQubeGame.registerAction("test1", RpsActionType.ROCK);
    rpsQubeGame.registerAction("test2", RpsActionType.ROCK);
    Assertions.assertThat(rpsQubeGame.getResultType()).isEqualTo(ResultType.TIE);
  }
}
