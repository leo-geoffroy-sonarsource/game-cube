package org.qubee.rps;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.qubee.data.ActionType;

public enum RpsActionType implements ActionType {
  @JsonProperty("ROCK")
  ROCK(false),
  @JsonProperty("PAPER")
  PAPER(false),
  @JsonProperty("ROCK")
  SCISSORS(false),
  @JsonProperty("WELL")
  WELL(false),
  @JsonProperty("PLUS1SECOND")
  PLUS1SECOND(true),
  @JsonProperty("MIDDLEFINGER")
  MIDDLEFINGER(false);

  private final boolean passThroughOnly;

  RpsActionType(boolean passThroughOnly) {

    this.passThroughOnly = passThroughOnly;
  }

  public boolean winAgainst(ActionType actionType) {
    if (this.equals(actionType)){
      //We don't win against itself, it's a tie
      return false;
    }
    if (this.equals(ROCK) && actionType.equals(SCISSORS)) {
      return true;
    }

    if (this.equals(PAPER) && (actionType.equals(ROCK) || actionType.equals(WELL))) {
      return true;
    }

    if ( this.equals(SCISSORS) && actionType.equals(PAPER)){
      return true;
    }
    if (this.equals(WELL) && (actionType.equals(SCISSORS) || actionType.equals(ROCK))) {
      return true;
    }
    if (this.equals(MIDDLEFINGER)){
      return false;
    }
    return actionType.equals(MIDDLEFINGER);
  }

  public boolean isPassThroughOnly() {
    return passThroughOnly;
  }
}
