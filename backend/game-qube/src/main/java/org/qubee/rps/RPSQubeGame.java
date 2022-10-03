package org.qubee.rps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.qubee.QubeGame;
import org.qubee.ResultType;
import org.qubee.data.ActionType;
import org.qubee.data.GameType;

public class RPSQubeGame implements QubeGame {

  public static final int GAME_TIMEOUT = 5;

  private Map<String, Status> opponents = new HashMap<>();
  private ResultType resultType;
  private String winner;


  public RPSQubeGame() {
  }

  @Override
  public Integer timeout() {
    return GAME_TIMEOUT;
  }

  public List<String> getParticipants() {

    return Collections.unmodifiableList(new ArrayList<>(opponents.keySet()));
  }

  @Override
  public void addOpponent(String username) {
    opponents.put(username, new Status());
  }

  @Override
  public GameType getGameType() {
    return GameType.ROCKPAPERSCISSORS;
  }

  @Override
  public void registerAction(String username, ActionType actionType) {
    RpsActionType rpsActionType = (RpsActionType) actionType;
    opponents.get(username).setRpsActionType(rpsActionType);
    String opponent = getOpponent(username);
    if (opponents.get(opponent).isDefined()) {
      RpsActionType previousAction = opponents.get(opponent).getRpsActionType();
      findWinner();
    }
  }

  private String getOpponent(String username) {
    return opponents.entrySet().stream().filter(o -> !o.getKey().equals(username))
      .map(o -> o.getKey())
      .findFirst().get();
  }

  private void findWinner() {
    Iterator<Map.Entry<String, Status>> opponentIterator = opponents.entrySet().iterator();
    String opponent1 = opponentIterator.next().getKey();
    String opponent2 = opponentIterator.next().getKey();

    if (isTimeout(opponent1) && isTimeout(opponent2)){
      resultType = ResultType.TIE;
      return;
    }else if (isTimeout(opponent1)){
      resultType = ResultType.WINNER;
      winner = opponent2;
      return;
    }else if (isTimeout(opponent2)){
      resultType = ResultType.WINNER;
      winner = opponent1;
      return;
    }
    if (opponents.get(opponent1).getRpsActionType() == opponents.get(opponent2).getRpsActionType()) {
      this.resultType = ResultType.TIE;
    } else if (opponents.get(opponent1).getRpsActionType().winAgainst(opponents.get(opponent2).getRpsActionType())) {
      this.resultType = ResultType.WINNER;
      this.winner = opponent1;
    } else {
      this.resultType = ResultType.WINNER;
      this.winner = opponent2;
    }
  }

  private boolean isTimeout(String opponent1) {
    return opponents.get(opponent1).isTimeout() != null && opponents.get(opponent1).isTimeout();
  }

  @Override
  public void registerTimeout(String username) {
    opponents.get(username).setTimeout(true);
    String opponent = getOpponent(username);
    if (opponents.get(opponent).isDefined()) {
      findWinner();
    }
  }

  @Override
  public ResultType getResultType() {
    return resultType;
  }

  @Override
  public String getWinner() {
    return winner;
  }

  private static class Status {
    ;
    private Boolean timeout;

    private RpsActionType rpsActionType;

    Status() {
    }

    public void setTimeout(boolean timeout) {
      this.timeout = timeout;
    }

    public void setRpsActionType(RpsActionType rpsActionType) {
      this.rpsActionType = rpsActionType;
    }

    public Boolean isTimeout() {
      return timeout;
    }

    public RpsActionType getRpsActionType() {
      return rpsActionType;
    }

    public boolean isDefined(){
      return this.timeout != null || this.rpsActionType != null;
    }
  }
}
