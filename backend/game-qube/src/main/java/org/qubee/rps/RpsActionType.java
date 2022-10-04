package org.qubee.rps;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.qubee.data.ActionType;

public enum RpsActionType implements ActionType {
  @JsonProperty("ROCK")
  ROCK,
  @JsonProperty("PAPER")
  PAPER,
  @JsonProperty("ROCK")
  SCISSORS,

  @JsonProperty("WELL")
  WELL;

  public boolean winAgainst(ActionType actionType) {
    if (this.equals(WELL) && (actionType.equals(SCISSORS) || actionType.equals(ROCK))){
      return true;
    }

    if (this.equals(ROCK) && actionType.equals(SCISSORS)) {
      return true;
    }

    if (this.equals(PAPER) && (actionType.equals(ROCK) || actionType.equals(WELL))) {
      return true;
    }

    return this.equals(SCISSORS) && actionType.equals(PAPER);
  }
}
