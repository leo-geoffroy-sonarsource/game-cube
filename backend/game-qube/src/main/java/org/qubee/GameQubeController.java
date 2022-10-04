package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.TimeoutMessage;
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
  List<String> waitingRoom = new ArrayList<>();

  List<QubeGame> games = new ArrayList<>();


  public GameQubeController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.rpsSender = new RpsSender(objectMapper);
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {

    LOG.info(username + " is connecting to the game");
    if (waitingRoom.contains(username)) {
      LOG.info("Username is already registered");
      rpsSender.sendErrorMessage(username, session, "Username " + username + " is already registered");
      return;
    }
    sessions.put(username, session);
    if (waitingRoom.size() == 2) {
      LOG.info("There are already 2 users registered: " + sessions.keySet());
      rpsSender.sendErrorMessage(username, session, "There are already 2 users registered: " + sessions.keySet());
      return;
    }

    waitingRoom.add(username);

    if (waitingRoom.size() == 2) {
      LOG.info("Two players in, starting the game");
      RPSQubeGame game = new RPSQubeGame();
      waitingRoom
        .forEach(game::addParticipant);
      rpsSender.broadcastStart(filterParticipants(sessions, game), game);
      waitingRoom.clear();
      games.add(game);

    }
  }

  private Map<String, Session> filterParticipants(Map<String, Session> sessions, QubeGame game) {
    return sessions.entrySet().stream()
      .filter(e -> game.getParticipants().contains(e.getKey()))
      .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    LOG.info(username + " is disconnected, removing from the game");
    removeUser(username);
  }

  private void removeUser(String username) {
    sessions.remove(username);
    if (sessions.isEmpty()) {
      findGameByParticipant(username)
        .ifPresent(g -> {
          LOG.info(username + " closing game");
          games.remove(g);
        });
    }
  }

  @OnError
  public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
    LOG.info(username + " had an error, Error: " + throwable.getMessage());
    removeUser(username);
  }

  @OnMessage
  public void onMessage(Session session, String message, @PathParam("username") String username) {
    QubeGame game = findGameByParticipant(username)
      .orElseThrow(() -> {
        rpsSender.sendErrorMessage(username, session, "Game does not exist");
        throw new IllegalArgumentException("Game does not exist");
      });
    try {
      LOG.info("Received a message from " + username + " Message: " + message);
      Message gameMessage = objectMapper.readValue(message, Message.class);

      if (gameMessage instanceof PlayerActionMessage) {
        game.registerAction(username, RpsActionType.valueOf(((PlayerActionMessage) gameMessage).getAction()));
        rpsSender.broadcastAction(filterParticipants(sessions, game), username, (PlayerActionMessage) gameMessage);
        if (game.getResultType() != null) {
          rpsSender.broadcastResult(filterParticipants(sessions, game), game);
          games.remove(game);
        }

      } else if (gameMessage instanceof TimeoutMessage) {
        game.registerTimeout(username);
      } else {
        rpsSender.sendErrorMessage(username, session, "Unrecognized message format");
      }
    } catch (JsonProcessingException e) {
      rpsSender.sendErrorMessage(username, session, "Cannot read message " + e.getMessage());
    }

  }

  private Optional<QubeGame> findGameByParticipant(String username) {
    return games.stream()
      .filter(g -> g.getParticipants().contains(username))
      .findFirst();
  }


}
