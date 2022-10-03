import { useState } from "react";
import styled from "styled-components";

export default function Timer() {
  const [percent, setPercent] = useState(0);

  setTimeout(() => (percent === 100 ? clearTimeout(100) : setPercent(percent + 1)), 100);

  return (
    <Container>
      <Bar>
        <Progress percent={percent} />
      </Bar>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  justify-content: center;
`;

const Bar = styled.div`
  border-radius: 8px;
  width: 500px;
  height: 10px;
  border: 1px solid black;
  background-color: transparent;
`;

const Progress = styled.div`
  border-radius: 8px;
  background-color: red;
  width: ${({ percent }) => percent}%;
  height: 100%;
`;
