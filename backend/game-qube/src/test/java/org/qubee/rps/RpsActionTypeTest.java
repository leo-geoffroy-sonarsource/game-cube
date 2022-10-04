package org.qubee.rps;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpsActionTypeTest {



  @Test
  public void test(){
    assertWinAgainst(RpsActionType.ROCK,RpsActionType.SCISSORS);
    assertWinAgainst(RpsActionType.PAPER,RpsActionType.ROCK);
    assertWinAgainst(RpsActionType.SCISSORS,RpsActionType.PAPER);
    assertWinAgainst(RpsActionType.WELL,RpsActionType.SCISSORS);
    assertWinAgainst(RpsActionType.WELL,RpsActionType.ROCK);
    assertWinAgainst(RpsActionType.PAPER,RpsActionType.WELL);
    assertWinAgainst(RpsActionType.ROCK,RpsActionType.SCISSORS);


    assertWinAgainst(RpsActionType.ROCK,RpsActionType.MIDDLEFINGER);
    assertWinAgainst(RpsActionType.PAPER,RpsActionType.MIDDLEFINGER);
    assertWinAgainst(RpsActionType.SCISSORS,RpsActionType.MIDDLEFINGER);
    assertWinAgainst(RpsActionType.WELL,RpsActionType.MIDDLEFINGER);
  }

  public void assertWinAgainst(RpsActionType action1, RpsActionType action2){
    Assertions.assertThat(action1.winAgainst(action2)).isTrue();
    Assertions.assertThat(action2.winAgainst(action1)).isFalse();
  }
}
