package org.qubee.rps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

  public List<String> getOpponents() {

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
    String opponent = getOpponent(username);
    opponents.get(username).setRpsActionType(rpsActionType);
    if (opponents.get(opponent).isDefined()) {
      RpsActionType previousAction = opponents.get(opponent).getRpsActionType();
      findWinner(username, rpsActionType, previousAction, opponent);
    }
  }

  private String getOpponent(String username) {
    return opponents.entrySet().stream().filter(o -> !o.getKey().equals(username))
      .map(o -> o.getKey())
      .findFirst().get();
  }

  private void findWinner(String username, RpsActionType rpsActionType, RpsActionType previousAction, String opponent) {
    if (opponents.get(opponent).isTimeout()!= null && opponents.get(opponent).isTimeout()){
      if (opponents.get(opponent).isTimeout()!= null && opponents.get(username).isTimeout()){
        this.resultType = ResultType.TIE;
      }else{
        this.resultType = ResultType.WINNER;
        this.winner = username;
      }
    }
    if (rpsActionType == previousAction) {
      this.resultType = ResultType.TIE;
    } else if (rpsActionType.winAgainst(previousAction)) {
      this.resultType = ResultType.WINNER;
      this.winner = username;
    } else {
      this.resultType = ResultType.WINNER;
      this.winner = opponent;
    }
  }

  @Override
  public void registerTimeout(String username) {
    opponents.get(username).setTimeout(true);
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
