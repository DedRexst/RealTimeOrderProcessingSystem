package com.project.orderservice.service;

import com.project.orderservice.event.OrderNotificationEvent;
import com.project.orderservice.event.OrderStatusChangedEvent;

/**
 * One committed status transition, described for both of its audiences: the customer email and the
 * analytics webhook. Built inside the transaction that performed the transition — the payloads read
 * LAZY associations — and published by the caller once that transaction has committed.
 *
 * @param notification what NotificationService renders as email
 * @param analytics what AnalyticsService forwards as a webhook
 */
public record OrderStatusChange(
    OrderNotificationEvent notification,
    OrderStatusChangedEvent analytics
) {

  public Long orderId() {
    return analytics.getOrderId();
  }
}
