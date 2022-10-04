import React from "react";
import styled from "styled-components";
import {
  MessageTypes,
  onServerAction,
  onServerConnected,
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

export default class App extends React.Component {
  state = {
    opponentName: undefined,
    opponentHand: undefined,
    userName: undefined,
    userHand: undefined,
    bonus: undefined,
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
    onServerAction((data) => {
      switch (data.type) {
        case MessageTypes.Start:
          this.setState(
            { opponentName: data.opponent, gameDuration: data.timeout, bonus: this.getBonus() },
            () => this.startTimer()
          );
          break;

        case MessageTypes.PlayerAction:
          this.setState({ opponentHand: data.action });
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
    onServerConnected(() => {
      console.log("Connected!");
    });
  };

  startTimer = () => {
    const time = 50;
    const incr = 100 / ((this.state.gameDuration * 1000) / time);

    const callBack = () => {
      const { timerPercentage, userHand } = this.state;

      if (timerPercentage < 100) {
        this.setState(
          ({ timerPercentage }) => ({ timerPercentage: timerPercentage + incr }),
          () => {
            setTimeout(callBack, time);
          }
        );
      } else if (userHand === undefined) {
        this.handleTimeOut();
      }
    };

    setTimeout(callBack, time);
  };

  getBonus = () => {
    return Math.random() >= 0.9
      ? Math.random() >= 0.5
        ? RockPaperScissorsActions.Well
        : RockPaperScissorsActions.Add1Second
      : undefined;
  };

  handleTimeOut = () => {
    this.setState({ timeOut: true });
    sendTimeout();
  };

  handleSetUserName = (userName) => {
    this.setState({ userName });
    this.setUpWebSocket(userName);
  };

  handleSetUserHand = (hand) => {
    const { userHand, timeOut } = this.state;
    if (userHand === undefined && !timeOut) {
      this.setState({ userHand: hand });
      sendPlayerAction(hand);
    }
  };

  handleNext = () => {
    this.setState({
      result: undefined,
      opponentName: undefined,
      opponentHand: undefined,
      userHand: undefined,
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

    if (opponentName === undefined) {
      const sortedPlayers = players.sort((a, b) => b.score - a.score);

      return (
        <Container>
          <h1>Waiting on Opponents</h1>
          <ol>
            {sortedPlayers.map((player) => (
              <li>
                {player.username} (W: {player.score})
              </li>
            ))}
          </ol>
          <Rules>
            Shortcuts:
            <ul>
              <li>r || left -> ✊ Rock </li>
              <li>p || down -> 🖐️ Paper</li>
              <li>s || right -> ✌️ Scissors</li>
              <li>b || up -> bonus (if applicable)</li>
            </ul>
            <p>Or you can click on the emoji like your grandma would ;) </p>
            <p>/!\ Once played, you CAN NOT change it!!</p>
            <p>Bonus:</p>
            <p>1 chance out of 10 to get a random bonus!</p>
            <ul>
              <li>Well 👌</li>
              <li>+1 sec 🕑</li>
              <li>MF 🖕 (WIP)</li>
              <li>Qube (WIP)</li>
            </ul>
          </Rules>

          {(userName === "Wouter" || userName === "Leo" || userName === "Guillaume") && (
            <button onClick={this.handleReady}>Ready!</button>
          )}
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
          setUserHand={this.handleSetUserHand}
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
  h2.WINNER {
    color: green;
  }
  h2.LOSER {
    color: red;
  }
  h2.TIE {
    color: black;
  }
`;

const Rules = styled.div`
  padding: 20px;
  margin: 20px;
  border: 2px solid black;
  border-radius: 4px;
  background-color: lightgray;
`;
