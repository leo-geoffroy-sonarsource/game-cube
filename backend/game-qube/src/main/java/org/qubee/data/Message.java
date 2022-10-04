package org.qubee.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.qubee.data.message.PlayerActionMessage;
import org.qubee.data.message.ResultMessage;
import org.qubee.data.message.StartMessage;
import org.qubee.data.message.TimeoutMessage;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PlayerActionMessage.class, name = "PLAYERACTION"),
  @JsonSubTypes.Type(value = TimeoutMessage.class, name = "TIMEOUT"),
  @JsonSubTypes.Type(value = StartMessage.class, name = "START"),
  @JsonSubTypes.Type(value = ResultMessage.class, name = "RESULT")

})
public abstract class Message {
}
