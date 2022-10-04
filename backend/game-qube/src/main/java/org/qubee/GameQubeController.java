package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import org.qubee.data.message.ReadyMessage;
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

  private final GamesManagement gamesManagement;
  private final SessionManager sessionManager;
  List<String> waitingRoom = new ArrayList<>();

  ScheduledExecutorService executor = Executors.newScheduledThreadPool(40);


  public GameQubeController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.gamesManagement = new GamesManagement();
    this.sessionManager = new SessionManager();
    this.rpsSender = new RpsSender(sessionManager, objectMapper);
  }


  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {

    sessionManager.put(username, session);
    LOG.info(username + " is connecting to the game");
    joinWaitingRoom(username);
  }

  private void joinWaitingRoom(String username) {
    Optional<QubeGame> existingGame = gamesManagement.findGameByParticipant(username);
    if (existingGame.isPresent()) {
      reportError("Username" + username + " is already in a game", username);
      return;
    }
    if (waitingRoom.contains(username)) {
      reportError("Username " + username + " is already registered", username);
      return;
    }
    if (waitingRoom.size() == 2) {
      reportError("There are already 2 users registered: " + waitingRoom, username);
      return;
    }

    LOG.info("Adding " + username + " to waiting room");
    waitingRoom.add(username);

    if (waitingRoom.size() == 2) {
      LOG.info("Two players in, starting the game");
      startGame();
    }
  }

  private void startGame() {
    RPSQubeGame game = new RPSQubeGame();
    waitingRoom
      .forEach(game::addParticipant);
    rpsSender.broadcastStart(game);
    executor.schedule(getTimeoutTask(game), game.timeout() + 1, TimeUnit.SECONDS);
    waitingRoom.clear();
    gamesManagement.addGame(game);
  }

  private Runnable getTimeoutTask(RPSQubeGame game) {
    return () -> {
      if (game.getResultType() == null) {
        game.getUnresolvedParticipants()
          .forEach(game::registerTimeout);
        proceedIfGameIsResolved(game);
      }
    };
  }

  private void reportError(String message, String username) {
    LOG.info(message);
    rpsSender.sendErrorMessage(username, message);
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    LOG.info(username + " is disconnected, removing from the game");
    removeUser(username);
  }

  private void removeUser(String username) {
    sessionManager.remove(username);
    if (sessionManager.isEmpty()) {
      gamesManagement.findGameByParticipant(username)
        .ifPresent(g -> {
          LOG.info(username + " closing game");
          gamesManagement.removeGame(g);
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
        QubeGame game;
        try{
          game = gamesManagement.findGame(username);
        }catch (Exception e){
          reportError("Game does not exist", username);
          return;
        }
        game.registerAction(username, RpsActionType.valueOf((playerActionMessage).getAction()));
        rpsSender.broadcastAction(game, username, playerActionMessage);
        proceedIfGameIsResolved(game);
      } else if (gameMessage instanceof TimeoutMessage) {
        QubeGame game = gamesManagement.findGame(username);
        game.registerTimeout(username);
        proceedIfGameIsResolved(game);
      } else if (gameMessage instanceof JoinMessage) {
        joinWaitingRoom(username);
      } else if (gameMessage instanceof ReadyMessage) {
        startGame();
      } else {
        reportError("Unrecognized message format", username);
      }
    } catch (JsonProcessingException e) {
      reportError("Cannot read message " + e.getMessage(), username);
    } catch (GameNotFoundException ex) {
      reportError("Game was not found", username);

    }
  }

  private void proceedIfGameIsResolved(QubeGame game) {
    if (game.getResultType() != null) {
      rpsSender.broadcastResult(game);
      gamesManagement.removeGame(game);
    }
  }


}
