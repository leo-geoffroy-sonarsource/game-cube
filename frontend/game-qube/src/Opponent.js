import styled from "styled-components";
import Hand from "./Hand";

export default function Opponent({ name, hand }) {
  return (
    <Container>
      <h2>{name}</h2>
      <Rotate>
        <Hand outcome={hand} />
      </Rotate>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
`;

const Rotate = styled.div`
  transform: rotate(270deg);
`;
