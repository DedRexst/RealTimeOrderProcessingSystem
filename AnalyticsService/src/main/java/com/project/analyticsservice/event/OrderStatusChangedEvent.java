package com.project.analyticsservice.event;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Consumer-side copy of the event OrderService publishes to orders.status-changed. Statuses are
 * Strings so a status added upstream does not break deserialization here.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

  private Long orderId;
  private Long customerId;
  private String previousStatus;
  private String newStatus;
  private String currency;
  private List<Long> productIds;
  private String reason;
  private String changedBy;
  private String externalReference;
  private Instant occurredAt;
}
