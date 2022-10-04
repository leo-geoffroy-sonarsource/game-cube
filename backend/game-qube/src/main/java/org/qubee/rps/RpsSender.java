package org.qubee.rps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.websocket.Session;
import org.jboss.logging.Logger;
import org.qubee.QubeGame;
import org.qubee.data.Message;
import org.qubee.data.message.ErrorMessage;
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.ResultMessage;
import org.qubee.data.message.StartMessage;

public class RpsSender {

  private static final Logger LOG = Logger.getLogger(RpsSender.class);

  private final ObjectMapper objectMapper;


  public RpsSender(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void broadcastStart(Map<String, Session> sessions, QubeGame qubeGame) {
    sessions.forEach((username, session) -> {
      String opponent = qubeGame.getParticipants().stream().filter(o -> !o.equals(username)).findFirst().orElseThrow(() -> {
        throw new IllegalArgumentException("No opponent found");
      });
      StartMessage startMessage = new StartMessage(opponent, qubeGame.timeout(), qubeGame.getGameType());
      sendMessage(username, session, startMessage);
    });
  }

  public void broadcastAction(Map<String, Session> sessions, String initiator, PlayerActionMessage playerActionMessage) {
    sessions.forEach((username, session) -> {
      if (!username.equals(initiator)) {
        sendMessage(username, session, playerActionMessage);
      }
    });
  }

  public void broadcastResult(Map<String, Session> sessions, QubeGame qubeGame) {
    sessions.forEach((username, session) -> {
      ResultMessage resultMessage = new ResultMessage(qubeGame.getResultType(), qubeGame.getWinner());
      sendMessage(username, session, resultMessage);
    });
  }

  public void sendErrorMessage(String username, Session session, String errorMessage) {
    ErrorMessage message = new ErrorMessage(errorMessage);
    sendMessage(username, session, message);
  }

  private void sendMessage(String username, Session session, Message message) {
    try {
      session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(message), result -> {
        if (result.getException() != null) {
          LOG.info("Unable to send message: " + result.getException());
        }
      });
    } catch (JsonProcessingException ex) {
      LOG.info("Unable to send message to " + username + " : " + ex.getMessage());
    }
  }
}
