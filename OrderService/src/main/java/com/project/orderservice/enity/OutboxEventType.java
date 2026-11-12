package com.project.orderservice.enity;

/** What an outbox row carries, which tells the relay how to read its payload and where to send it. */
public enum OutboxEventType {
  ORDER_CREATED,
  ORDER_NOTIFICATION,
  PRODUCT_CREATED
}
