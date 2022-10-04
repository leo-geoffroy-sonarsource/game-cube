package org.qubee.data.message;

import org.qubee.data.Message;

public class ErrorMessage extends Message {
  public String message;

  public ErrorMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
