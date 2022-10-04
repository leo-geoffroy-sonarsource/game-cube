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
  PLUS1SECOND(true);
  private final boolean passThroughOnly;

  RpsActionType(boolean passThroughOnly) {

    this.passThroughOnly = passThroughOnly;
  }

  public boolean winAgainst(ActionType actionType) {
    if (this.equals(WELL) && (actionType.equals(SCISSORS) || actionType.equals(ROCK))) {
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

  public boolean isPassThroughOnly() {
    return passThroughOnly;
  }
}
