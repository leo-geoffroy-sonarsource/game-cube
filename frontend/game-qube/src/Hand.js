export default function Hand({ outcome = "hand" }) {
  let className = outcome;
  if (outcome !== "hand") {
    className += " hand done";
  }
  return (
    <div className={className} id="user-hand">
      <div className="fist"></div>
      <div className="finger finger-1"></div>
      <div className="finger finger-2"></div>
      <div className="finger finger-3"></div>
      <div className="finger finger-4"></div>
      <div className="thumb"></div>
      <div className="arm"></div>
    </div>
  );
}
