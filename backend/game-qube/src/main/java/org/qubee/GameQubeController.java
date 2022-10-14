package org.qubee;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.collections4.ListUtils;
import org.jboss.logging.Logger;
import org.qubee.data.message.JoinMessage;
import org.qubee.data.message.Message;
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.ReadyMessage;
import org.qubee.data.message.TimeoutMessage;
import org.qubee.exceptions.GameNotFoundException;
import org.qubee.rps.RPSQubeGame;
import org.qubee.rps.RpsActionType;
import org.qubee.rps.RpsSender;

@ServerEndpoint("/game-qube/participate/{username}")
@ApplicationScoped
public class GameQubeController {
  private static final Logger LOG = Logger.getLogger(GameQubeController.class);

  private final ObjectMapper objectMapper;
  private final RpsSender rpsSender;

  private final GamesManagement gamesManagement;
  private final SessionManager sessionManager;
  private final List<String> waitingRoom = new ArrayList<>();


  ScheduledExecutorService executor = Executors.newScheduledThreadPool(40);


  public GameQubeController(ObjectMapper objectMapper, GamesManagement gamesManagement) {
    this.objectMapper = objectMapper;
    this.gamesManagement = gamesManagement;
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

    LOG.info("Adding " + username + " to waiting room");
    waitingRoom.add(username);
    rpsSender.broadcastLobby(waitingRoom, gamesManagement.getScores());
  }

  private void startGame() {
    List<String> participants = new ArrayList<>(waitingRoom);
    Collections.shuffle(participants);
    ListUtils.partition(participants, 2)
      .forEach(e -> {
        if (e.size() == 2) {
          RPSQubeGame game = new RPSQubeGame();
          e.forEach(game::addParticipant);
          rpsSender.broadcastStart(game);
          executor.schedule(getTimeoutTask(game), game.timeout() + 4, TimeUnit.SECONDS);
          gamesManagement.addGame(game);
          waitingRoom.removeAll(e);
        }
      });
    rpsSender.broadcastLobby(waitingRoom, gamesManagement.getScores());
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
    LOG.error(message);
    rpsSender.sendErrorMessage(username, message);
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    LOG.info(username + " is disconnected, removing from the game and waiting room");
    removeUser(username);
  }

  private void removeUser(String username) {
    sessionManager.remove(username);
    waitingRoom.remove(username);
    rpsSender.broadcastLobby(waitingRoom, gamesManagement.getScores());
  }

  @OnError
  public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
    LOG.info(username + " had an error, Error: " + throwable.getMessage());
    //removeUser(username);
  }

  @OnMessage
  public void onMessage(Session session, String message, @PathParam("username") String username) {

    try {
      LOG.info(username + ": Received a message -> " + message);
      Message gameMessage = objectMapper.readValue(message, Message.class);

      if (gameMessage instanceof PlayerActionMessage playerActionMessage) {
        QubeGame game;
        try {
          game = gamesManagement.findGame(username);
        } catch (Exception e) {
          reportError("Game does not exist", username);
          return;
        }
        RpsActionType actionType = RpsActionType.valueOf((playerActionMessage).getAction());
        rpsSender.broadcastParticipantAction(game, username, playerActionMessage);
        if (!actionType.isPassThroughOnly()) {
          game.registerAction(username, actionType);
          proceedIfGameIsResolved(game);
        }
      } else if (gameMessage instanceof TimeoutMessage) {
        QubeGame game = gamesManagement.findGame(username);
        game.registerTimeout(username);
        proceedIfGameIsResolved(game);
      } else if (gameMessage instanceof JoinMessage) {
        joinWaitingRoom(username);
      } else if (gameMessage instanceof ReadyMessage) {
        if (waitingRoom.size() > 1) {
          LOG.info("Starting game for " + waitingRoom.size() + " participants");
        } else {
          reportError("Not enough participants to run a game (" + waitingRoom.size() + ")", username);
        }
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
      if (game.getWinner() != null) {
        gamesManagement.incrementScore(game.getWinner());
      }
      gamesManagement.removeGame(game);
    }
  }


}
