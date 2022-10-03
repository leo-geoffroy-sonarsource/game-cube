import styled from "styled-components";
import Hand from "./Hand";

export default function Opponent() {
  return (
    <Container>
      <Rotate>
        <Hand />
      </Rotate>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  justify-content: center;
`;

const Rotate = styled.div`
  transform: rotate(270deg);
`;
