package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import org.qubee.data.Message;
import org.qubee.data.PlayerActionMessage;
import org.qubee.data.TimeoutMessage;
import org.qubee.rps.RPSQubeGame;
import org.qubee.rps.RpsActionType;
import org.qubee.rps.RpsSender;

@ServerEndpoint("/game-qube/{username}")
@ApplicationScoped
public class GameQubeController {
  private static final Logger LOG = Logger.getLogger(GameQubeController.class);

  private final ObjectMapper objectMapper;
  private final RpsSender rpsSender;
  private final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private QubeGame game;


  public GameQubeController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.rpsSender = new RpsSender(objectMapper);
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {
    LOG.info(username + " is connecting to the game");
    if (sessions.containsKey(username)) {
      LOG.info("Username is already registered");
      return;
      //throw new IllegalArgumentException("Username" + username + " already exists in the game");
    }
    if (sessions.size() == 2) {
      LOG.info("There are already 2 users registered: " + sessions.keySet());
    }

    sessions.put(username, session);

    if (sessions.size() == 2) {
      LOG.info("Two players in, starting the game");
      game = new RPSQubeGame();
      sessions.keySet()
        .forEach(u -> game.addOpponent(u));
      rpsSender.broadcastStart(sessions, game);
    }
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    LOG.info(username + " is disconnected, removing from the game");
    sessions.remove(username);
    if (sessions.isEmpty()) {
      game = null;
    }
  }

  @OnError
  public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
    LOG.info(username + " had an error, Error: " + throwable.getMessage());
    sessions.remove(username);
  }

  @OnMessage
  public void onMessage(Session session, String message, @PathParam("username") String username) {

    if (game == null) {
      throw new IllegalArgumentException("Game was not started yet");
    }
    try {
      LOG.info("Received a message from " + username + " Message: " + message);
      Message gameMessage = objectMapper.readValue(message, Message.class);

      if (gameMessage instanceof PlayerActionMessage) {
        game.registerAction(username, RpsActionType.valueOf(((PlayerActionMessage) gameMessage).getAction()));
        rpsSender.broadcastAction(sessions, game, username, (PlayerActionMessage) gameMessage);
        if (game.getResultType() != null) {
          rpsSender.broadcastResult(sessions, game);
        }
      } else if (gameMessage instanceof TimeoutMessage) {
        game.registerTimeout(username);
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while parsing data", e);
    }

  }


}
