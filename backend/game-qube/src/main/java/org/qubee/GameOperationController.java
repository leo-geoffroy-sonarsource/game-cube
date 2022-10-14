package org.qubee;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

@Path("/game-qube/scores")
public class GameOperationController {

  private final GamesManagement gamesManagement;

  public GameOperationController(GamesManagement gamesManagement) {
    this.gamesManagement = gamesManagement;
  }

  @DELETE
  public void deleteScores() {
    gamesManagement.clearScore();
  }
}
