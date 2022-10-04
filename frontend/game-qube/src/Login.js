import React from "react";
import styled from "styled-components";

export default function Login(props) {
  const [name, setName] = React.useState("");

  return (
    <Container>
      <form
        onSubmit={() => {
          props.setUserName(name);
        }}>
        <input
          placeholder="Your username"
          value={name}
          onChange={(event) => setName(event.target.value)}
        />
        <button type="submit">Save</button>
      </form>
    </Container>
  );
}

const Container = styled.div`
  margin-top: 100px;
  display: flex;
  justify-content: center;
  align-items: center;

  form {
    display: flex;
    flex-direction: column;
  }

  input,
  button {
    text-align: center;
    padding: 10px 20px;
    margin-bottom: 20px;
  }
`;
