let socket;
let gameType;

function timeoutMessage() {
  return JSON.stringify({
    type: MessageTypes.Timeout,
  });
}

function joinMessage() {
  return JSON.stringify({
    type: MessageTypes.Join,
  });
}

function readyMessage() {
  return JSON.stringify({
    type: MessageTypes.Ready,
  });
}

function playerActionMessage(action) {
  validateAction(action);
  return JSON.stringify({
    type: MessageTypes.PlayerAction,
    action,
  });
}

function parseServerMessage(payload) {
  console.log("RECEIVED PAYLOAD:", payload);
  const { type, ...data } = JSON.parse(payload);

  if (typeof type !== "string") {
    throw new Error("Received type is not a string");
  }

  switch (type) {
    case MessageTypes.Start:
      return { type, ...parseStartGameMessage(data) };

    case MessageTypes.PlayerAction:
      return { type, ...parseOpponentActionMessage(data) };

    case MessageTypes.Result:
      return { type, ...parseResultMessage(data) };

    case MessageTypes.Lobby:
      return { type, ...parseLobbyMessage(data) };

    case MessageTypes.Error:
      console.error(data.message);
      return { type, ...data };

    default:
      throw new Error(`Unkown message type: ${type}`);
  }
}

function parseStartGameMessage({ opponent, timeout, game }) {
  if (
    typeof opponent !== "string" ||
    typeof timeout !== "number" ||
    game !== GameTypes.RockPaperScissors
  ) {
    throw new Error(
      `Couldn't start game with received data. Opponent: ${opponent}, timeout: ${timeout}, game: ${game}`
    );
  }

  gameType = game;
  return { opponent: decodeURIComponent(opponent), timeout, game };
}

function parseOpponentActionMessage({ action }) {
  validateAction(action);
  return { action };
}

function parseResultMessage({ result, winner }) {
  switch (result) {
    case ResultTypes.Tie:
      return { result };

    case ResultTypes.Winner:
      if (typeof winner !== "string") {
        throw new Error(`Unexpected winner: ${winner}`);
      }
      return { result, winner: decodeURIComponent(winner) };

    default:
      throw new Error(`Unkown result: ${result}`);
  }
}

function parseLobbyMessage({ players }) {
  if (players === undefined || !Array.isArray(players)) {
    throw new Error(`Expected "players" to be an array`);
  }

  return {
    players: players.map(({ username, score }, i) => {
      if (typeof username !== "string" || typeof score !== "number") {
        throw new Error(`Unexpected data for player ${i}: username: ${username}, score: ${score}`);
      }
      return { username: decodeURIComponent(username), score };
    }),
  };
}

function validateAction(action) {
  switch (gameType) {
    case GameTypes.RockPaperScissors:
      if (!Object.values(RockPaperScissorsActions).includes(action)) {
        throw new Error(`Unkown action: ${action}`);
      }
      break;

    default:
      break;
  }
}

export const MessageTypes = {
  Error: "ERROR",
  Join: "JOIN",
  Ready: "READY",
  Lobby: "LOBBY",
  Timeout: "TIMEOUT",
  PlayerAction: "PLAYERACTION",
  Start: "START",
  Result: "RESULT",
};

export const ResultTypes = {
  Winner: "WINNER",
  Tie: "TIE",
};

export const GameTypes = {
  RockPaperScissors: "ROCKPAPERSCISSORS",
};

export const RockPaperScissorsActions = {
  Rock: "ROCK",
  Paper: "PAPER",
  Scissors: "SCISSORS",
  Well: "WELL",
  Add1Second: "PLUS1SECOND",
  MiddleFinger: "MIDDLEFINGER",
};

export function registerNewPlayer(username) {
  if (socket && socket.readyState < 3) {
    throw new Error("There is already an active connection");
  }

  let url = window.location.host+"/game-qube/participate";
  socket = new WebSocket(`ws:///${url}/${username}`);
}

export function terminateGame() {
  try {
    socket.close();
  } catch (e) {
    // noop
  }
}

export function sendTimeout() {
  console.log("SEND TIMEOUT:", timeoutMessage());
  socket.send(timeoutMessage());
}

export function sendReady() {
  console.log("SEND READY:", readyMessage());
  socket.send(readyMessage());
}

export function sendPlayerAction(action) {
  console.log("SEND ACTION:", playerActionMessage(action));
  socket.send(playerActionMessage(action));
}

export function sendJoin() {
  console.log("SEND JOIN:", joinMessage());
  socket.send(joinMessage());
}

export function onServerConnected(cb) {
  socket.addEventListener("open", cb);
}

export function onServerDisconnected(cb) {
  socket.addEventListener("close", cb);
}

export function onServerAction(cb) {
  socket.addEventListener("message", ({ data }) => {
    cb(parseServerMessage(data));
  });
}
