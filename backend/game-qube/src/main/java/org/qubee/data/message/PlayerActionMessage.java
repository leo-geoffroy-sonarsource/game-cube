package org.qubee.data.message;

public class PlayerActionMessage extends Message {
  private String action;

  public void setAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }
}
