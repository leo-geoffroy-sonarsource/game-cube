package org.qubee.data.message;

import org.qubee.ResultType;
import org.qubee.data.Message;

public class ResultMessage extends Message {
  ResultType result;
  String winner;

  public ResultMessage(ResultType result, String winner) {
    this.result = result;
    this.winner = winner;
  }

  public ResultType getResult() {
    return result;
  }

  public void setResult(ResultType result) {
    this.result = result;
  }

  public String getWinner() {
    return winner;
  }

  public void setWinner(String winner) {
    this.winner = winner;
  }
}
