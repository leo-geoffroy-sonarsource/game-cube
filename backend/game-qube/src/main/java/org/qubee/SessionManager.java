package org.qubee;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

public class SessionManager {

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();

  public void put(String username, Session session) {
    sessions.put(username, session);
  }

  public Session get(String username) {
    return sessions.get(username);
  }

  public Map<String, Session> getSessions() {
    return sessions;
  }

  public void remove(String username) {
    sessions.remove(username);
  }

  public boolean isEmpty() {
    return sessions.isEmpty();
  }
}
