package com.project.analyticsservice.service.impl;

import com.project.analyticsservice.dto.StatusAnalyticsUpdate;
import com.project.analyticsservice.dto.StatusFunnelDto;
import com.project.analyticsservice.dto.StatusReasonsDto;
import com.project.analyticsservice.dto.TransitionTimeDto;
import com.project.analyticsservice.event.OrderStatusChangedEvent;
import com.project.analyticsservice.service.OrderStatusAnalyticsService;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * In-memory, per-instance state: a restart starts from zero. orders.status-changed is keyed by
 * orderId, so every transition of one order lands on the same instance.
 *
 * <p>An event is identified by order, new status and {@code occurredAt}; one seen before changes
 * nothing, which makes listener redeliveries safe.
 */
@Service
public class OrderStatusAnalyticsServiceImpl implements OrderStatusAnalyticsService {

  static final int TOP_REASONS = 5;

  /** orderId → what this instance knows about that order. Least recently updated is evicted. */
  private final Map<Long, OrderTrack> orders;

  /** "FROM->TO" → durations of that transition. */
  private final Map<String, DurationStats> transitionTimes = new HashMap<>();

  /** status → distinct orders that have been in it. */
  private final Map<String, Long> ordersReached = new HashMap<>();

  /** status → reason → number of transitions into that status with that reason. */
  private final Map<String, Map<String, Long>> reasonsByStatus = new HashMap<>();

  private long ordersSeen;

  /**
   * @param maxTrackedOrders should comfortably exceed the number of orders in flight; an evicted
   *     order seen again counts as new and its next transition has no duration.
   */
  public OrderStatusAnalyticsServiceImpl(
      @Value("${analytics.status.max-tracked-orders:10000}") int maxTrackedOrders) {
    this.orders = new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Long, OrderTrack> eldest) {
        return size() > maxTrackedOrders;
      }
    };
  }

  @Override
  public synchronized Optional<StatusAnalyticsUpdate> record(OrderStatusChangedEvent event) {
    String from = event.getPreviousStatus();
    String to = event.getNewStatus();
    Instant at = event.getOccurredAt();

    OrderTrack track = trackOrder(event.getOrderId());
    if (!track.seenEvents.add(to + "|" + at)) {
      return Optional.empty();
    }

    TransitionTimeDto transitionTime = measureTransition(track, from, to, at);
    track.advance(to, at);

    // The previous status counts as reached too: the first transition seen is out of PENDING.
    markReached(track, from);
    markReached(track, to);

    StatusReasonsDto reasons = countReason(to, event.getReason());

    return Optional.of(new StatusAnalyticsUpdate(transitionTime, buildFunnel(), reasons));
  }

  private OrderTrack trackOrder(Long orderId) {
    OrderTrack track = orders.get(orderId);
    if (track == null) {
      track = new OrderTrack();
      orders.put(orderId, track);
      ordersSeen++;
    }
    return track;
  }

  private TransitionTimeDto measureTransition(OrderTrack track, String from, String to, Instant at) {
    boolean measurable = from != null && from.equals(track.lastStatus)
        && at != null && track.lastAt != null && !at.isBefore(track.lastAt);
    if (!measurable) {
      return null;
    }

    long millis = Duration.between(track.lastAt, at).toMillis();
    DurationStats stats = transitionTimes.computeIfAbsent(from + "->" + to,
        key -> new DurationStats());
    stats.add(millis);

    return new TransitionTimeDto(from, to, stats.count,
        toSeconds(stats.average()), toSeconds(stats.maxMillis), toSeconds(millis));
  }

  private void markReached(OrderTrack track, String status) {
    if (status != null && track.reached.add(status)) {
      ordersReached.merge(status, 1L, Long::sum);
    }
  }

  private StatusFunnelDto buildFunnel() {
    List<StatusFunnelDto.Stage> stages = ordersReached.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .map(entry -> new StatusFunnelDto.Stage(entry.getKey(), entry.getValue(),
            round(100.0 * entry.getValue() / ordersSeen)))
        .toList();
    return new StatusFunnelDto(ordersSeen, stages);
  }

  private StatusReasonsDto countReason(String status, String reason) {
    if (reason == null || reason.isBlank()) {
      return null;
    }

    Map<String, Long> counts = reasonsByStatus.computeIfAbsent(status, key -> new HashMap<>());
    counts.merge(reason.strip(), 1L, Long::sum);

    long total = counts.values().stream().mapToLong(Long::longValue).sum();
    List<StatusReasonsDto.ReasonCount> topReasons = counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()))
        .limit(TOP_REASONS)
        .map(entry -> new StatusReasonsDto.ReasonCount(entry.getKey(), entry.getValue()))
        .toList();
    return new StatusReasonsDto(status, total, topReasons);
  }

  private double toSeconds(double millis) {
    return round(millis / 1000.0);
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private static final class OrderTrack {
    private String lastStatus;
    private Instant lastAt;
    private final Set<String> reached = new HashSet<>();
    private final Set<String> seenEvents = new HashSet<>();

    void advance(String status, Instant at) {
      if (lastAt == null || at == null || !at.isBefore(lastAt)) {
        lastStatus = status;
        lastAt = at;
      }
    }
  }

  private static final class DurationStats {
    private long count;
    private long totalMillis;
    private long maxMillis;

    void add(long millis) {
      count++;
      totalMillis += millis;
      maxMillis = Math.max(maxMillis, millis);
    }

    double average() {
      return totalMillis / (double) count;
    }
  }
}
