package com.project.orderservice.event;

import com.project.orderservice.enity.Currency;
import com.project.orderservice.enity.Status;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The audit-shaped counterpart of {@link OrderNotificationEvent}: same transition, but addressed to
 * AnalyticsService rather than to the customer, so it carries what a downstream system needs (both
 * ends of the transition, who caused it) and none of the personal data an email needs.
 *
 * <p>AnalyticsService turns it into an outbound webhook.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

  private Long orderId;

  private Long customerId;

  /** Null only for a row written before Status.PENDING existed. */
  private Status previousStatus;

  private Status newStatus;

  private Currency currency;

  private List<Long> productIds;

  private String reason;

  private String changedBy;

  /**
   * The id of this transition's record in the third-party system that reported it, so a webhook
   * subscriber can reconcile against that system directly. Null for a change made here.
   */
  private String externalReference;

  private Instant occurredAt;
}
