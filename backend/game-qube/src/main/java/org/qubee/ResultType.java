package org.qubee;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResultType {
  @JsonProperty("WINNER")
  WINNER,
  @JsonProperty("TIE")
  TIE;
}
