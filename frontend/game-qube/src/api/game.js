let socket;
let gameType;

function timeoutMessage() {
  return JSON.stringify({
    type: MessageTypes.Timeout,
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
    case "START":
      return { type, ...parseStartGameMessage(data) };

    case "PLAYERACTION":
      return { type, ...parseOpponentActionMessage(data) };

    case "RESULT":
      return { type, ...parseResultMessage(data) };

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
  return { opponent, timeout: timeout * 1000, game };
}

function parseOpponentActionMessage({ action }) {
  validateAction(action);

  return { action };
}

function parseResultMessage({ result, winner }) {
  if (!Object.values(ResultTypes).includes(result)) {
    throw new Error(`Unkown result: ${result}`);
  }

  switch (result) {
    case ResultTypes.Tie:
      return { result };

    case ResultTypes.Winner:
      if (typeof winner !== "string") {
        throw new Error(`Unexpected winner: ${winner}`);
      }
      return { result, winner };
  }
}

function validateAction(action) {
  switch (gameType) {
    case GameTypes.RockPaperScissors:
      if (!Object.values(RockPaperScissorsActions).includes(action)) {
        throw new Error(`Unkown action: ${action}`);
      }
      break;
  }
}

export const MessageTypes = {
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
};

export function registerNewPlayer(username) {
  if (socket && socket.readyState < 3) {
    throw new Error("There is already an active connection");
  }

  socket = new WebSocket(`ws://leog.eu.ngrok.io/game-qube/${username}`);
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

export function sendPlayerAction(action) {
  console.log("SEND ACTION:", playerActionMessage(action));
  socket.send(playerActionMessage(action));
}

export function onServerConnected(cb) {
  socket.addEventListener("open", cb);
}

export function onServerAction(cb) {
  socket.addEventListener("message", ({ data }) => {
    cb(parseServerMessage(data));
  });
}
