import React from "react";
import styled from "styled-components";
import {
  MessageTypes,
  onServerAction,
  onServerConnected,
  registerNewPlayer,
  sendPlayerAction,
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
    timerPercentage: 0,
    gameDuration: 5,
    timeOut: false,
    result: undefined,
  };

  setUpWebSocket = (userName) => {
    registerNewPlayer(userName);
    onServerAction((data) => {
      switch (data.type) {
        case MessageTypes.Start:
          this.setState({ opponentName: data.opponent }, () => this.startTimer());

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

  componentWillUnmount() {
    terminateGame();
  }

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
    });
  };

  render() {
    const { userName, userHand, opponentName, opponentHand, timerPercentage, result } = this.state;
    if (!userName) {
      return <Login setUserName={this.handleSetUserName} />;
    }

    if (opponentName === undefined) {
      return <h2>Waiting on Opponent</h2>;
    }

    return (
      <div className="container">
        <Opponent name={opponentName} hand={opponentHand} />
        {result ? (
          <Result>
            <h3>{result}</h3>
            <button onClick={this.handleNext}>Next</button>
          </Result>
        ) : (
          <Timer timerPercentage={timerPercentage} />
        )}
        <Input userName={userName} userHand={userHand} setUserHand={this.handleSetUserHand} />
      </div>
    );
  }
}

const Result = styled.div`
  display: flex;
  align-items: center;
  flex-direction: column;
`;
