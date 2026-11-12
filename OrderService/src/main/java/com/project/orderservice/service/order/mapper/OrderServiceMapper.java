package com.project.orderservice.service.order.mapper;

import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Order;
import com.project.orderservice.enity.Product;
import com.project.orderservice.enity.Status;
import com.project.orderservice.event.OrderCreatedEvent;
import com.project.orderservice.event.OrderNotificationEvent;
import com.project.orderservice.event.OrderStatusChangedEvent;
import com.project.orderservice.service.StatusChangeDetails;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceMapper {


  /** The creation-time notification, which has no transition behind it to describe. */
  public OrderNotificationEvent entityToNotificationEvent(Order order) {
    return entityToNotificationEvent(order, null, null);
  }

  /**
   * Must be called inside the transaction that loaded the order: the customer and the product list
   * are LAZY, and this reads both to build a payload the consumer can render without calling back.
   */
  public OrderNotificationEvent entityToNotificationEvent(
      Order order, Status previousStatus, StatusChangeDetails details) {
    Customer customer = order.getCustomer();
    return OrderNotificationEvent.builder()
        .orderId(order.getId())
        .customerId(customer.getId())
        .customerEmail(customer.getEmail())
        .customerName(customer.getName())
        .status(order.getStatus())
        .previousStatus(previousStatus)
        .reason(details == null ? null : details.reason())
        .changedBy(details == null ? null : details.changedBy())
        .externalReference(details == null ? null : details.externalReference())
        .currency(order.getCurrency())
        .address(order.getOrderAddress())
        .products(order.getProducts().stream()
            .map(product -> OrderNotificationEvent.ProductLine.builder()
                .id(product.getId())
                .name(product.getName())
                .build())
            .toList())
        .occurredAt(occurredAt(details))
        .build();
  }

  public OrderCreatedEvent toOrderCreatedEvent(Order order) {
    return new OrderCreatedEvent(
        order.getId(),
        order.getCustomer().getId(),
        order.getProducts().stream().map(Product::getId).toList(),
        order.getCurrency()
    );
  }

  /**
   * The same transition as {@link #entityToNotificationEvent(Order, Status, StatusChangeDetails)},
   * minus
   * the customer's name and address: analytics and the webhook it fans out to have no use for
   * personal data, and leaving it out keeps it from travelling to a third party.
   */
  public OrderStatusChangedEvent entityToStatusChangedEvent(
      Order order, Status previousStatus, StatusChangeDetails details) {
    return OrderStatusChangedEvent.builder()
        .orderId(order.getId())
        .customerId(order.getCustomer().getId())
        .previousStatus(previousStatus)
        .newStatus(order.getStatus())
        .currency(order.getCurrency())
        .productIds(order.getProducts().stream().map(Product::getId).toList())
        .reason(details == null ? null : details.reason())
        .changedBy(details == null ? null : details.changedBy())
        .externalReference(details == null ? null : details.externalReference())
        .occurredAt(occurredAt(details))
        .build();
  }

  public Order setCustomerAndProducts(Order order, Customer customer, List<Product> products) {
    order.setCustomer(customer);
    order.setProducts(products);
    return order;
  }

  /**
   * When the transition happened, as opposed to when this payload is built. They differ only for a
   * change a third-party system reports after the fact, and both consumers show it to someone: the
   * email says when the order moved, and the webhook is what a subscriber orders events by.
   */
  private Instant occurredAt(StatusChangeDetails details) {
    if (details == null || details.occurredAt() == null) {
      return Instant.now();
    }
    return details.occurredAt();
  }
}
