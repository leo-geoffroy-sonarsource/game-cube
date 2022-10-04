package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import org.qubee.data.message.Message;
import org.qubee.data.message.JoinMessage;
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.TimeoutMessage;
import org.qubee.exceptions.GameNotFoundException;
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
  private final GamesManagement gameManagement;
  List<String> waitingRoom = new ArrayList<>();

  ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();



  public GameQubeController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.rpsSender = new RpsSender(objectMapper);
    this.gameManagement = new GamesManagement();
  }


  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {

    sessions.put(username, session);
    LOG.info(username + " is connecting to the game");
    joinWaitingRoom(session, username);
  }

  private void joinWaitingRoom(Session session, String username) {
    Optional<QubeGame> existingGame = gameManagement.findGameByParticipant(username);
    if (existingGame.isPresent()) {
      reportError("Username" + username + " is already in a game", username, session);
      return;
    }
    if (waitingRoom.contains(username)) {
      reportError("Username " + username + " is already registered", username, session);
      return;
    }
    if (waitingRoom.size() == 2) {
      reportError("There are already 2 users registered: " + sessions.keySet(), username, session);
      return;
    }

    LOG.info("Adding " + username + " to waiting room");
    waitingRoom.add(username);

    if (waitingRoom.size() == 2) {
      LOG.info("Two players in, starting the game");
      RPSQubeGame game = new RPSQubeGame();
      waitingRoom
        .forEach(game::addParticipant);
      rpsSender.broadcastStart(filterParticipants(sessions, game), game);
      executor.schedule(getTimeoutTask(game), game.timeout() + 1, TimeUnit.SECONDS);
      waitingRoom.clear();
      gameManagement.addGame(game);
    }
  }

  private Runnable getTimeoutTask(RPSQubeGame game) {
    return () -> {
      if (game.getResultType() == null) {
        game.getUnresolvedParticipants()
          .forEach(game::registerTimeout);
        if (game.getResultType() != null) {
          rpsSender.broadcastResult(filterParticipants(sessions, game), game);
          gameManagement.removeGame(game);
        }
      }

    };
  }

  private void reportError(String message, String username, Session session) {
    LOG.info(message);
    rpsSender.sendErrorMessage(username, session, message);
  }

  private static Map<String, Session> filterParticipants(Map<String, Session> sessions, QubeGame game) {
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
      gameManagement.findGameByParticipant(username)
        .ifPresent(g -> {
          LOG.info(username + " closing game");
          gameManagement.removeGame(g);
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

    try {
      LOG.info("Received a message from " + username + " Message: " + message);
      Message gameMessage = objectMapper.readValue(message, Message.class);

      if (gameMessage instanceof PlayerActionMessage playerActionMessage) {
        QubeGame game = gameManagement.findGame( username);
        game.registerAction(username, RpsActionType.valueOf((playerActionMessage).getAction()));
        rpsSender.broadcastAction(filterParticipants(sessions, game), username, playerActionMessage);
        if (game.getResultType() != null) {
          rpsSender.broadcastResult(filterParticipants(sessions, game), game);
          gameManagement.removeGame(game);
        }
      } else if (gameMessage instanceof TimeoutMessage) {
        QubeGame game = gameManagement.findGame(username);
        game.registerTimeout(username);
      } else if (gameMessage instanceof JoinMessage) {
        joinWaitingRoom(session, username);
      } else {
        reportError("Unrecognized message format", username, session);
      }
    } catch (JsonProcessingException e) {
      reportError("Cannot read message " + e.getMessage(), username, session);
    }catch (GameNotFoundException ex){
      reportError("Game was not found", username, session);

    }
  }



}
