package com.project.analyticsservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.project.analyticsservice.dto.StatusAnalyticsUpdate;
import com.project.analyticsservice.dto.StatusFunnelDto;
import com.project.analyticsservice.event.OrderStatusChangedEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderStatusAnalyticsServiceImplTest {

  private static final Instant T0 = Instant.parse("2026-11-08T10:00:00Z");

  private final OrderStatusAnalyticsServiceImpl analytics = new OrderStatusAnalyticsServiceImpl(100);

  @Test
  void measuresTimeBetweenTransitionsOfTheSameOrder() {
    record(1L, "PENDING", "RESERVED", T0, null);
    StatusAnalyticsUpdate update = record(1L, "RESERVED", "SUCCESSFUL", T0.plusSeconds(30), null);

    assertEquals("RESERVED", update.transitionTime().fromStatus());
    assertEquals("SUCCESSFUL", update.transitionTime().toStatus());
    assertEquals(30.0, update.transitionTime().lastSeconds());
    assertEquals(1, update.transitionTime().samples());
  }

  @Test
  void averagesAcrossOrders() {
    record(1L, "PENDING", "RESERVED", T0, null);
    record(2L, "PENDING", "RESERVED", T0, null);
    record(1L, "RESERVED", "SUCCESSFUL", T0.plusSeconds(10), null);
    StatusAnalyticsUpdate update = record(2L, "RESERVED", "SUCCESSFUL", T0.plusSeconds(30), null);

    assertEquals(2, update.transitionTime().samples());
    assertEquals(20.0, update.transitionTime().avgSeconds());
    assertEquals(30.0, update.transitionTime().maxSeconds());
  }

  @Test
  void firstTransitionSeenHasNoDuration() {
    assertNull(record(1L, "PENDING", "RESERVED", T0, null).transitionTime());
  }

  @Test
  void redeliveredEventChangesNothing() {
    record(1L, "PENDING", "RESERVED", T0, null);
    OrderStatusChangedEvent again = event(1L, "PENDING", "RESERVED", T0, null);

    assertTrue(analytics.record(again).isEmpty());
    StatusAnalyticsUpdate next = record(2L, "PENDING", "FAILED", T0, "out of stock");
    assertEquals(2, next.funnel().totalOrders());
    assertEquals(1, stage(next.funnel(), "RESERVED").orders());
  }

  @Test
  void funnelCountsDistinctOrdersPerStatusIncludingTheStartingOne() {
    record(1L, "PENDING", "RESERVED", T0, null);
    record(1L, "RESERVED", "SUCCESSFUL", T0.plusSeconds(5), null);
    record(2L, "PENDING", "RESERVED", T0, null);
    StatusAnalyticsUpdate update = record(2L, "RESERVED", "RELEASED", T0.plusSeconds(5), "cancelled");

    StatusFunnelDto funnel = update.funnel();
    assertEquals(2, funnel.totalOrders());
    assertEquals(100.0, stage(funnel, "PENDING").sharePercent());
    assertEquals(100.0, stage(funnel, "RESERVED").sharePercent());
    assertEquals(50.0, stage(funnel, "SUCCESSFUL").sharePercent());
    assertEquals(50.0, stage(funnel, "RELEASED").sharePercent());
  }

  @Test
  void ranksReasonsPerStatus() {
    record(1L, "PENDING", "FAILED", T0, "out of stock");
    record(2L, "PENDING", "FAILED", T0, "payment declined");
    StatusAnalyticsUpdate update = record(3L, "PENDING", "FAILED", T0, " out of stock ");

    assertEquals("FAILED", update.reasons().status());
    assertEquals(3, update.reasons().totalWithReason());
    assertEquals("out of stock", update.reasons().topReasons().get(0).reason());
    assertEquals(2, update.reasons().topReasons().get(0).count());
  }

  @Test
  void transitionWithoutReasonReportsNoReasons() {
    assertNull(record(1L, "PENDING", "RESERVED", T0, "  ").reasons());
  }

  private StatusAnalyticsUpdate record(Long orderId, String from, String to, Instant at, String reason) {
    return analytics.record(event(orderId, from, to, at, reason)).orElseThrow();
  }

  private static OrderStatusChangedEvent event(Long orderId, String from, String to,
      Instant at, String reason) {
    return new OrderStatusChangedEvent(orderId, 7L, from, to, "USD", List.of(1L), reason,
        "system", null, at);
  }

  private static StatusFunnelDto.Stage stage(StatusFunnelDto funnel, String status) {
    return funnel.stages().stream().filter(s -> s.status().equals(status)).findFirst()
        .orElseThrow();
  }
}
