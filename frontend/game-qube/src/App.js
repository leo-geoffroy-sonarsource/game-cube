import "./App.css";
import Input from "./Input";
import Opponent from "./Opponent";
import Timer from "./Timer";

function App() {
  return (
    <div className="container">
      <Opponent />
      <Timer />
      <Input />
    </div>
  );
}

export default App;
