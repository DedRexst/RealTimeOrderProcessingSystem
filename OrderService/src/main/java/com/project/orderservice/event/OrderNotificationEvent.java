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
 * Everything NotificationService needs to compose the customer email, carried in the event itself.
 * The consumer never calls back into OrderService, so a notification can still be delivered while
 * OrderService is down, and a replayed record renders exactly the email that was originally sent
 * rather than one reflecting the order's later state.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationEvent {

  private Long orderId;

  private Long customerId;

  private String customerEmail;

  private String customerName;

  private Status status;

  /** Where the order came from, so the email can read as a change rather than a bare state. */
  private Status previousStatus;

  /** Why an operator moved the order, when they said. Rendered verbatim; null when absent. */
  private String reason;

  /** The operator login or service name behind the change. */
  private String changedBy;

  /**
   * The id of this transition's record in the system that reported it — a shipment, a payment —
   * so the customer can quote it back and support can find it. Null when the change originated
   * here rather than in a third-party system.
   */
  private String externalReference;

  private Currency currency;

  private String address;

  private List<ProductLine> products;

  /** When the status change happened, so a delayed email can say so honestly. */
  private Instant occurredAt;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductLine {

    private Long id;

    private String name;
  }
}
