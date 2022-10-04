import React, { useEffect } from "react";
import styled from "styled-components";
import { RockPaperScissorsActions } from "./api/game";
import Hand from "./Hand";
import "./Input.css";

export default function Input({ userName, userHand, sendAction, bonus }) {
  const handleKeyDown = (e) => {
    if (e.key === "ArrowLeft" || e.key === "r") {
      sendAction(RockPaperScissorsActions.Rock);
    } else if (e.key === "ArrowDown" || e.key === "p") {
      sendAction(RockPaperScissorsActions.Paper);
    } else if (e.key === "ArrowRight" || e.key === "s") {
      sendAction(RockPaperScissorsActions.Scissors);
    } else if (e.key === "ArrowUp" || e.key === "b") {
      if (bonus === RockPaperScissorsActions.Well) {
        sendAction(RockPaperScissorsActions.Well);
      } else if (bonus === RockPaperScissorsActions.Add1Second) {
        sendAction(RockPaperScissorsActions.Add1Second);
      }
    }
  };

  useEffect(() => {
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  });

  return (
    <Container>
      <Rotate>
        <Hand outcome={userHand} />
      </Rotate>
      <h2>{userName}</h2>
      <Options>
        <Option onClick={() => sendAction(RockPaperScissorsActions.Rock)}>✊</Option>
        <Option onClick={() => sendAction(RockPaperScissorsActions.Paper)}>🖐️</Option>
        <Option onClick={() => sendAction(RockPaperScissorsActions.Scissors)}>✌️</Option>
        {bonus === RockPaperScissorsActions.Well && (
          <Option onClick={() => sendAction(RockPaperScissorsActions.Well)}>👌</Option>
        )}
        {bonus === RockPaperScissorsActions.Add1Second && (
          <Option onClick={() => sendAction(RockPaperScissorsActions.Add1Second)}>+1</Option>
        )}
      </Options>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const Options = styled.div`
  display: flex;
  justify-content: center;
`;

const Option = styled.div`
  font-size: 50px;
  vertical-align: middle;
  line-height: 2;
  margin-right: 25px;
`;

const Rotate = styled.div`
  transform: rotate(90deg);
`;
