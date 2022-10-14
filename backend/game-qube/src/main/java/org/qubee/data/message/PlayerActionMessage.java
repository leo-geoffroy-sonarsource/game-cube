package org.qubee.data.message;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PlayerActionMessage extends Message {
  private String action;

  public void setAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }
}
