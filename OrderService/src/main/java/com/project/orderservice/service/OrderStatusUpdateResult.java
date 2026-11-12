package com.project.orderservice.service;

import com.project.orderservice.enity.Order;
import java.util.Optional;

/**
 * The order as it now stands, plus the transition to announce — empty when the request asked for
 * the status the order already had. Separating the two lets the endpoint answer 200 with the
 * current order either way while emailing the customer only about changes that really happened, so
 * a retried admin call cannot produce a second email.
 */
public record OrderStatusUpdateResult(
    Order order,
    Optional<OrderStatusChange> change
) {

  public static OrderStatusUpdateResult unchanged(Order order) {
    return new OrderStatusUpdateResult(order, Optional.empty());
  }

  public static OrderStatusUpdateResult changed(Order order, OrderStatusChange change) {
    return new OrderStatusUpdateResult(order, Optional.of(change));
  }
}
