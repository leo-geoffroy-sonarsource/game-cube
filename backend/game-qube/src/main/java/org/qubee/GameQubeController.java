package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.qubee.data.Message;
import org.qubee.data.PlayerActionMessage;
import org.qubee.data.ResultMessage;
import org.qubee.data.StartMessage;
import org.qubee.data.TimeoutMessage;

@ServerEndpoint("/game-qube/{username}")
@ApplicationScoped
public class GameQubeController {
  private static final Logger LOG = Logger.getLogger(GameQubeController.class);

  private final ObjectMapper objectMapper;
  Map<String, Session> sessions = new ConcurrentHashMap<>();
  private QubeGame game;


  public GameQubeController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {
    if (sessions.containsKey(username)) {
      throw new IllegalArgumentException("Username" + username + " already exists in the game");
    }
    LOG.info(username + " is connected to the game");
    sessions.put(username, session);

    if (sessions.size() == 2) {
      LOG.info("Two players in, starting the game");
      game = new RPSQubeGame();
      sessions.keySet().stream()
        .forEach(u -> game.addOpponent(u));
      broadcastStart(game);
    }
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    LOG.info(username + " is disconnected, removing from the game");
    sessions.remove(username);
  }

  @OnError
  public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
    LOG.info(username + " is disconnected, removing from the game");
    sessions.remove(username);
  }

  @OnMessage
  public void onMessage(Session session, String message, @PathParam("username") String username) {
    try {
      LOG.info("Received a message from " + username + " Message: " + message);
      Message gameMessage = objectMapper.readValue(message, Message.class);

      if (gameMessage instanceof PlayerActionMessage) {

      } else if (gameMessage instanceof ResultMessage) {


      } else if (gameMessage instanceof TimeoutMessage) {


      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while parsing data", e);
    }

  }

  private void broadcastStart(QubeGame qubeGame) {
    sessions.entrySet().forEach(e -> {
      String username = e.getKey();
      Session session = e.getValue();

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

}
