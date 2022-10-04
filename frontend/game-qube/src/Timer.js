import styled from "styled-components";

export default function Timer({ timerPercentage }) {
  return (
    <Container>
      <Bar>
        <Progress percent={timerPercentage} />
      </Bar>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  justify-content: center;
  margin: 30px 0;
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
