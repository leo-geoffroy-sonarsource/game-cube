import styled from "styled-components";
import Hand from "./Hand";

export default function Opponent() {
  return (
    <Container>
      <Hand />
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  justify-content: center;
`;
