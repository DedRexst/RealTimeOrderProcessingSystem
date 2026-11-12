package com.project.orderservice.producer;

import com.project.orderservice.event.OrderNotificationEvent;
import com.project.orderservice.event.OrderStatusChangedEvent;
import com.project.orderservice.service.OrderStatusChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Announces a committed status transition to both of its audiences.
 *
 * <p>Callers hand it an {@link OrderStatusChange} only after the transaction that performed the
 * transition has committed, so a customer is never emailed about a status that was rolled back.
 *
 * <p>Nothing here throws. The status change is already durable by the time this runs, and neither
 * audience is worth failing the caller over: an HTTP client would see a 500 for an order that did
 * change, and a Kafka listener would redeliver an event whose transition is now a no-op, looping
 * without ever sending the message. The two sends are also independent — an unreachable analytics
 * topic must not cost the customer their email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangePublisher {

  private final OrderNotificationProducer orderNotificationProducer;
  private final OrderStatusEventProducer orderStatusEventProducer;

  public void publish(OrderStatusChange change) {
    publishNotification(change.notification());
    publishStatusChanged(change.analytics());
  }

  private void publishNotification(OrderNotificationEvent notification) {
    try {
      orderNotificationProducer.publishOrderNotification(notification)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Order {} is {} but its notification was not delivered",
                  notification.getOrderId(), notification.getStatus(), ex);
            }
          });
    } catch (Exception e) {
      log.error("Order {} is {} but its notification could not be queued",
          notification.getOrderId(), notification.getStatus(), e);
    }
  }

  private void publishStatusChanged(OrderStatusChangedEvent event) {
    try {
      orderStatusEventProducer.publishOrderStatusChanged(event)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Order {} moved to {} but the analytics event was not delivered",
                  event.getOrderId(), event.getNewStatus(), ex);
            }
          });
    } catch (Exception e) {
      log.error("Order {} moved to {} but the analytics event could not be queued",
          event.getOrderId(), event.getNewStatus(), e);
    }
  }
}
