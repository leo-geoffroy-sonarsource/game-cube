package org.qubee.rps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.websocket.Session;
import org.jboss.logging.Logger;
import org.qubee.QubeGame;
import org.qubee.data.PlayerActionMessage;
import org.qubee.data.ResultMessage;
import org.qubee.data.StartMessage;

public class RpsSender {

  private static final Logger LOG = Logger.getLogger(RpsSender.class);

  private final ObjectMapper objectMapper;


  public RpsSender(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void broadcastStart(Map<String, Session> sessions, QubeGame qubeGame) {
    sessions.forEach((username, session) -> {

      String opponent = qubeGame.getOpponents().stream().filter(o -> !o.equals(username))
        .findFirst().orElseThrow(() -> {
          throw new IllegalArgumentException("No opponent found");
        });
      StartMessage startMessage = new StartMessage(opponent, qubeGame.timeout(), qubeGame.getGameType());

      try {
        session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(startMessage), result -> {
          if (result.getException() != null) {
            LOG.info("Unable to send message: " + result.getException());
          }
        });
      } catch (JsonProcessingException ex) {
        LOG.info("Unable to send message to " + username + " : " + ex.getMessage());
      }
    });
  }

  public void broadcastAction(Map<String, Session> sessions, QubeGame qubeGame, String initiator, PlayerActionMessage playerActionMessage) {
    sessions.forEach((username, session) -> {

      if (!username.equals(initiator)) {
        try {
          session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(playerActionMessage), result -> {
            if (result.getException() != null) {
              LOG.info("Unable to send message: " + result.getException());
            }
          });
        } catch (JsonProcessingException ex) {
          LOG.info("Unable to send message to " + username + " : " + ex.getMessage());
        }
      }
    });
  }

  public void broadcastResult(Map<String, Session> sessions, QubeGame qubeGame) {
    sessions.forEach((username, session) -> {

      ResultMessage resultMessage = new ResultMessage(qubeGame.getResultType(), qubeGame.getWinner());
      try {
        session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(resultMessage), result -> {
          if (result.getException() != null) {
            LOG.info("Unable to send message: " + result.getException());
          }
        });
      } catch (JsonProcessingException ex) {
        LOG.info("Unable to send message to " + username + " : " + ex.getMessage());
      }
    });
  }
}
