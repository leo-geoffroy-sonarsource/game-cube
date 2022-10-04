import React from "react";
import styled from "styled-components";
import {
  MessageTypes,
  onServerAction,
  onServerConnected,
  onServerDisconnected,
  registerNewPlayer,
  RockPaperScissorsActions,
  sendJoin,
  sendPlayerAction,
  sendReady,
  sendTimeout,
  terminateGame,
} from "./api/game";
import "./App.css";
import Input from "./Input";
import Login from "./Login";
import Opponent from "./Opponent";
import Timer from "./Timer";

const TIMEOUT_LOOP = 50;

export default class App extends React.Component {
  state = {
    connected: false,
    opponentName: undefined,
    opponentHand: undefined,
    userName: undefined,
    userHand: undefined,
    bonus: undefined,
    usedBonus: false,
    timerPercentage: 0,
    gameDuration: 5,
    timeOut: false,
    result: undefined,
    players: [],
  };

  componentWillUnmount() {
    terminateGame();
  }

  setUpWebSocket = (userName) => {
    registerNewPlayer(userName);

    onServerConnected(() => {
      this.setState({ connected: true });
    });

    onServerDisconnected(() => {
      this.setState({ connected: false });
    });

    onServerAction((data) => {
      switch (data.type) {
        case MessageTypes.Start:
          this.setState(
            { opponentName: data.opponent, gameDuration: data.timeout, bonus: this.getBonus() },
            () => this.startTimer()
          );
          break;

        case MessageTypes.PlayerAction:
          if (data.action === RockPaperScissorsActions.Add1Second) {
            this.handleAdd1SecondAction();
          } else {
            this.setState({ opponentHand: data.action });
          }
          break;

        case MessageTypes.Result:
          if (data.winner === this.state.userName) {
            this.setState({ result: "WINNER" });
          } else if (data.winner === this.state.opponentName) {
            this.setState({ result: "LOSER" });
          } else {
            this.setState({ result: "TIE" });
          }
          break;

        case MessageTypes.Lobby:
          this.setState({ players: data.players });
          break;

        default:
          return;
      }
    });
  };

  startTimer = () => {
    const incr = 100 / ((this.state.gameDuration * 1000) / TIMEOUT_LOOP);

    const callBack = () => {
      const { timerPercentage, userHand } = this.state;

      if (timerPercentage < 100) {
        this.setState(
          ({ timerPercentage }) => ({ timerPercentage: timerPercentage + incr }),
          () => {
            setTimeout(callBack, TIMEOUT_LOOP);
          }
        );
      } else if (userHand === undefined) {
        this.handleTimeOut();
      }
    };

    setTimeout(callBack, TIMEOUT_LOOP);
  };

  getBonus = () => {
    if (Math.random() >= 0.8) {
      const r = Math.random();
      if (r <= 0.3) {
        return RockPaperScissorsActions.Well;
      }
      if (r <= 0.6) {
        return RockPaperScissorsActions.Add1Second;
      }
      return RockPaperScissorsActions.MiddleFinger;
    }
    return undefined;
  };

  handleTimeOut = () => {
    this.setState({ timeOut: true });
    sendTimeout();
  };

  handleSetUserName = (userName) => {
    this.setState({ userName });
    this.setUpWebSocket(userName);
  };

  handleSendAction = (hand) => {
    const { userHand, timeOut, usedBonus } = this.state;
    if (userHand === undefined && !timeOut) {
      if (hand === RockPaperScissorsActions.Add1Second && !usedBonus) {
        this.handleAdd1SecondAction();
        this.setState({ usedBonus: true });
      } else {
        this.setState({ userHand: hand });
      }
      sendPlayerAction(hand);
    }
  };

  handleAdd1SecondAction = () => {
    this.setState(({ timerPercentage, gameDuration }) => ({
      timerPercentage: timerPercentage - 100 / gameDuration,
    }));
  };

  handleNext = () => {
    this.setState({
      result: undefined,
      opponentName: undefined,
      opponentHand: undefined,
      userHand: undefined,
      bonus: undefined,
      usedBonus: false,
      timerPercentage: 0,
      timeOut: false,
    });
    sendJoin();
  };

  handleReady = () => {
    sendReady();
  };

  render() {
    const {
      userName,
      connected,
      userHand,
      opponentName,
      opponentHand,
      timerPercentage,
      result,
      players,
      bonus,
    } = this.state;
    if (!userName) {
      return <Login setUserName={this.handleSetUserName} />;
    }

    if (!connected) {
      return <Container>Connecting...</Container>;
    }

    if (opponentName === undefined) {
      const sortedPlayers = players.sort((a, b) => b.score - a.score);

      return (
        <Container>
          <h1>Waiting for opponents</h1>
          <ol>
            {sortedPlayers.map((player) => (
              <li key={player.username}>
                {player.username} (W: {player.score})
              </li>
            ))}
          </ol>

          {(userName === "Wouter" || userName === "Leo" || userName === "Guillaume") && (
            <button onClick={this.handleReady}>Start Game!</button>
          )}

          <Rules>
            Shortcuts:
            <ul>
              <li>r || left -> ✊ Rock </li>
              <li>p || down -> 🖐️ Paper</li>
              <li>s || right -> ✌️ Scissors</li>
              <li>b || up -> bonus (if applicable)</li>
            </ul>
            <p>Or you can click on the emoji like your grandma would 👵</p>
            <p>/!\ Once played, you CANNOT change it!!</p>
            <p>Bonus:</p>
            <p>80% chance to get a random bonus!</p>
            <ul>
              <li>Well 👌 (beats Rock and Scissors)</li>
              <li>+1s 🕑 (extend timeout by 1 second)</li>
              <li>MF 🖕 (Really?)</li>
              <li>Qubee (Coming Soon!)</li>
            </ul>
          </Rules>
        </Container>
      );
    }

    return (
      <div className="container">
        <Opponent name={opponentName} hand={opponentHand} />
        {result ? (
          <Container>
            <h2 className={result}>{result}</h2>
            <button onClick={this.handleNext}>Next</button>
          </Container>
        ) : (
          <Timer timerPercentage={timerPercentage} />
        )}
        <Input
          userName={userName}
          userHand={userHand}
          sendAction={this.handleSendAction}
          bonus={bonus}
        />
      </div>
    );
  }
}

const Container = styled.div`
  display: flex;
  align-items: center;
  flex-direction: column;
  margin: 30px 0;

  h2.WINNER {
    color: green;
  }
  h2.LOSER {
    color: red;
  }
  h2.TIE {
    color: black;
  }

  button {
    margin-top: 20px;
    padding: 10px 20px;
  }
`;

const Rules = styled.div`
  padding: 20px;
  margin: 20px;
  border: 2px solid black;
  border-radius: 4px;
  background-color: lightgray;
`;
