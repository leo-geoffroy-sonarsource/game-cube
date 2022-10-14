package org.qubee.data.message;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PlayerWithScore {

  private String username;

  private Integer score;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }
}
