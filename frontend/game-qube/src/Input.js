import React, { useEffect } from "react";
import styled from "styled-components";
import { RockPaperScissorsActions } from "./api/game";
import Hand from "./Hand";
import "./Input.css";

export default function Input({ userName, userHand, setUserHand, bonus }) {
  const handleKeyUp = (e) => {
    if (e.key === "ArrowLeft" || e.key === "r") {
      setUserHand(RockPaperScissorsActions.Rock);
    } else if (e.key === "ArrowDown" || e.key === "p") {
      setUserHand(RockPaperScissorsActions.Paper);
    } else if (e.key === "ArrowRight" || e.key === "s") {
      setUserHand(RockPaperScissorsActions.Scissors);
    } else if (e.key === "ArrowUp" || e.key === "b") {
      if (bonus === RockPaperScissorsActions.Well) {
        setUserHand(RockPaperScissorsActions.Well);
      }
    }
  };

  useEffect(() => {
    document.addEventListener("keyup", handleKeyUp);
    return () => document.removeEventListener("keyup", handleKeyUp);
  });

  return (
    <Container>
      <Rotate>
        <Hand outcome={userHand} />
      </Rotate>
      <h2>{userName}</h2>
      <Options>
        <Option onClick={() => setUserHand(RockPaperScissorsActions.Rock)}>✊</Option>
        <Option onClick={() => setUserHand(RockPaperScissorsActions.Paper)}>🖐️</Option>
        <Option onClick={() => setUserHand(RockPaperScissorsActions.Scissors)}>✌️</Option>
        {bonus === RockPaperScissorsActions.Well && (
          <Option onClick={() => setUserHand(RockPaperScissorsActions.Well)}>👌</Option>
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
