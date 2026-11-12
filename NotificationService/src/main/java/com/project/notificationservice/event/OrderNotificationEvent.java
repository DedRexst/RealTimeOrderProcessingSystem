package com.project.notificationservice.event;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Consumer-side mirror of the event OrderService publishes to orders.notifications.
 *
 * <p>Deliberately a separate class rather than a shared module: the producer sends no type headers,
 * so the two sides are coupled by field names only. {@code status} and {@code currency} are Strings
 * here because this service has no reason to know OrderService's enums, and an unknown value should
 * render in an email rather than fail deserialization.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationEvent {

  private Long orderId;

  private Long customerId;

  private String customerEmail;

  private String customerName;

  private String status;

  /** The status the order left; null for the creation email, which has no transition behind it. */
  private String previousStatus;

  /** Free text an operator supplied with a manual status change. */
  private String reason;

  /** Operator login or service name behind the change; not shown to the customer. */
  private String changedBy;

  /**
   * The id of the change's record in the third-party system that reported it — a shipment, a
   * payment. Unlike changedBy this is shown: it is the number a customer quotes when they write
   * back. Null when the change was made inside the company.
   */
  private String externalReference;

  private String currency;

  private String address;

  private List<ProductLine> products;

  private Instant occurredAt;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductLine {

    private Long id;

    private String name;
  }
}
