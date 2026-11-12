package com.project.analyticsservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.analyticsservice.util.StatsMessageType;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Pushes analytics to every open dashboard.
 *
 * <p>Keeps the latest value of each aggregate and a short tail of recent events, and replays them
 * to a newly connected dashboard so it does not sit empty until the next order arrives.
 */
@Slf4j
@Component
public class StatsWebSocketHandler extends TextWebSocketHandler {

  /** Recent events replayed to a new dashboard. */
  private static final int EVENT_TAIL_SIZE = 50;

  /** Bound on remembered aggregates; the least recently updated is dropped first. */
  private static final int SNAPSHOT_LIMIT = 500;

  private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
  private final Map<String, String> snapshot = new LinkedHashMap<>();
  private final Deque<String> eventTail = new ArrayDeque<>();

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * Sends one message to every open dashboard.
   *
   * @param snapshotKey identifies the aggregate (customer, currency, product…) so a newer value
   *     replaces the older one in the replay; null for an event, which goes to the event tail.
   */
  public void publish(StatsMessageType type, String snapshotKey, Object data) {
    String payload;
    try {
      payload = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
    } catch (JsonProcessingException e) {
      log.error("Could not serialize a {} message for the dashboard", type, e);
      return;
    }
    remember(type, snapshotKey, payload);
    sessions.forEach(session -> send(session, payload));
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessions.add(session);
    log.info("Dashboard connected: {} ({} open)", session.getId(), sessions.size());
    currentBacklog().forEach(payload -> send(session, payload));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    sessions.remove(session);
    log.info("Dashboard disconnected: {} ({} open)", session.getId(), sessions.size());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.debug("Transport error on dashboard session {}", session.getId(), exception);
    drop(session);
  }

  public int openSessions() {
    return sessions.size();
  }

  private synchronized void remember(StatsMessageType type, String snapshotKey, String payload) {
    if (snapshotKey == null) {
      eventTail.addLast(payload);
      while (eventTail.size() > EVENT_TAIL_SIZE) {
        eventTail.removeFirst();
      }
      return;
    }

    String key = type + "|" + snapshotKey;
    snapshot.remove(key);
    snapshot.put(key, payload);
    while (snapshot.size() > SNAPSHOT_LIMIT) {
      snapshot.remove(snapshot.keySet().iterator().next());
    }
  }

  private synchronized List<String> currentBacklog() {
    List<String> backlog = new ArrayList<>(snapshot.size() + eventTail.size());
    backlog.addAll(snapshot.values());
    backlog.addAll(eventTail);
    return backlog;
  }

  private void send(WebSocketSession session, String payload) {
    if (!session.isOpen()) {
      sessions.remove(session);
      return;
    }
    try {
      // A WebSocketSession is not safe for concurrent senders: Streams threads, the listener and
      // a connecting client's replay can all reach the same session at once.
      synchronized (session) {
        session.sendMessage(new TextMessage(payload));
      }
    } catch (IOException | IllegalStateException e) {
      log.debug("Dropping dashboard session {}: {}", session.getId(), e.getMessage());
      drop(session);
    }
  }

  private void drop(WebSocketSession session) {
    sessions.remove(session);
    try {
      session.close(CloseStatus.SERVER_ERROR);
    } catch (IOException ignored) {
      // Already closed.
    }
  }
}
