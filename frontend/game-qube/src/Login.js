import React from "react";
import styled from "styled-components";

export default function Login(props) {
  const [name, setName] = React.useState("");

  return (
    <Container>
      <input value={name} onChange={(event) => setName(event.target.value)} />
      <button
        onClick={() => {
          props.setUserName(name);
        }}>
        Save
      </button>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
`;
