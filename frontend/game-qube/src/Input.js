import React, { useEffect } from "react";
import styled from "styled-components";
import { RockPaperScissorsActions } from "./api/game";
import Hand from "./Hand";
import "./Input.css";

export default function Input({ userName, userHand, setUserHand }) {
  const handleKeyUp = (e) => {
    if (e.keyCode === 82 || e.keyCode === 37) {
      setUserHand(RockPaperScissorsActions.Rock);
    } else if (e.keyCode === 80 || e.keyCode === 38) {
      setUserHand(RockPaperScissorsActions.Paper);
    } else if (e.keyCode === 83 || e.keyCode === 39) {
      setUserHand(RockPaperScissorsActions.Scissors);
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
