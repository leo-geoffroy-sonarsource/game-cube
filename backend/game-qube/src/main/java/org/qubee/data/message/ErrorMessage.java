package org.qubee.data.message;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
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
