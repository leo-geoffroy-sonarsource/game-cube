import React, { useEffect, useState } from "react";
import styled from "styled-components";
import Hand from "./Hand";
import "./Input.css";

export default function Input() {
  const [outcome, setOutcome] = useState("hand");

  const handleKeyDown = (e) => {
    if (e.keyCode === 82 || e.keyCode === 37) {
      setOutcome("rock");
    } else if (e.keyCode === 80 || e.keyCode === 38) {
      setOutcome("paper");
    } else if (e.keyCode === 83 || e.keyCode === 39) {
      setOutcome("scissors");
    }
  };

  useEffect(() => {
    document.addEventListener("keyup", handleKeyDown);

    return () => document.removeEventListener("keyup", handleKeyDown);
  });

  return (
    <Container>
      <Rotate>
        <Hand outcome={outcome} />
      </Rotate>
      <Options>
        <Option type="radio" onClick={() => setOutcome("rock")}>
          ✊
        </Option>
        <Option type="radio" onClick={() => setOutcome("paper")}>
          🖐️
        </Option>
        <Option type="radio" onClick={() => setOutcome("scissors")}>
          ✌️
        </Option>
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
