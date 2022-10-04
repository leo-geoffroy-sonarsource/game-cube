package org.qubee;

import java.util.List;
import org.qubee.data.ActionType;
import org.qubee.data.GameType;

public interface QubeGame{

  Integer timeout();

  List<String> getParticipants();

  List<String> getUnresolvedParticipants();

  public void addParticipant(String username);

  GameType getGameType();

  public void registerAction(String username, ActionType actionType);

  public void registerTimeout(String username);

  ResultType getResultType();

  String getWinner();
}
