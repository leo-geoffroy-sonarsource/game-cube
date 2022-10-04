package org.qubee.rps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.websocket.Session;
import org.jboss.logging.Logger;
import org.qubee.QubeGame;
import org.qubee.SessionManager;
import org.qubee.data.message.LobbyMessage;
import org.qubee.data.message.Message;
import org.qubee.data.message.ErrorMessage;
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.ResultMessage;
import org.qubee.data.message.StartMessage;

public class RpsSender {

  private static final Logger LOG = Logger.getLogger(RpsSender.class);

  private final SessionManager sessionManager;
  private final ObjectMapper objectMapper;


  public RpsSender(SessionManager sessionManager, ObjectMapper objectMapper) {
    this.sessionManager = sessionManager;
    this.objectMapper = objectMapper;
  }

  public void broadcastStart(QubeGame qubeGame) {
    filterParticipants(qubeGame).forEach((username, session) -> {
      String opponent = qubeGame.getParticipants().stream().filter(o -> !o.equals(username)).findFirst().orElseThrow(() -> {
        throw new IllegalArgumentException("No opponent found");
      });
      StartMessage startMessage = new StartMessage(opponent, qubeGame.timeout(), qubeGame.getGameType());
      sendMessage(username, session, startMessage);
    });
  }

  public void broadcastAction(QubeGame qubeGame, String initiator, PlayerActionMessage playerActionMessage) {
    filterParticipants(qubeGame).forEach((username, session) -> {
      if (!username.equals(initiator)) {
        sendMessage(username, session, playerActionMessage);
      }
    });
  }

  public void broadcastResult(QubeGame qubeGame) {
    filterParticipants(qubeGame).forEach((username, session) -> {
      ResultMessage resultMessage = new ResultMessage(qubeGame.getResultType(), qubeGame.getWinner());
      sendMessage(username, session, resultMessage);
    });
  }

  public void sendErrorMessage(String username, String errorMessage) {
    ErrorMessage message = new ErrorMessage(errorMessage);
    sendMessage(username, sessionManager.get(username), message);
  }

  public void broadcastLobby(List<String> users, Map<String, Integer> scores) {
    LobbyMessage message = new LobbyMessage();
    users
      .forEach(e -> message.addPlayer(e, scores.getOrDefault(e, 0)));

    sessionManager.getSessions().entrySet()
      .stream()
      .filter(e -> users.contains(e.getKey()))
      .forEach(e -> sendMessage(e.getKey(), e.getValue(), message));
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

  private Map<String, Session> filterParticipants(QubeGame game) {
    return sessionManager.getSessions().entrySet().stream()
      .filter(e -> game.getParticipants().contains(e.getKey()))
      .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }


}
