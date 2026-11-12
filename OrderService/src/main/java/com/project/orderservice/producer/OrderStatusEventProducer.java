package com.project.orderservice.producer;

import com.project.orderservice.event.OrderStatusChangedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publishes order status transitions for AnalyticsService, which relays them as webhooks.
 *
 * <p>Keyed by orderId for the same reason as the notification topic: a subscriber must never see
 * an order's transitions out of order, and one partition per order is what guarantees that.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusEventProducer {

  @Value("${kafka.topics.orders-status-changed}")
  private String ordersStatusChangedTopic;

  private final KafkaTemplate<String, OrderStatusChangedEvent> kafkaTemplate;
  private final MeterRegistry meterRegistry;

  @Transactional("kafkaTransactionManager")
  public CompletableFuture<SendResult<String, OrderStatusChangedEvent>> publishOrderStatusChanged(
      OrderStatusChangedEvent event) {

    log.info("Publishing OrderStatusChangedEvent: orderId={}, {} -> {}, topic={}",
        event.getOrderId(), event.getPreviousStatus(), event.getNewStatus(),
        ordersStatusChangedTopic);

    return kafkaTemplate
        .send(ordersStatusChangedTopic, event.getOrderId().toString(), event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            meterRegistry.counter("kafka.producer.errors",
                "topic", ordersStatusChangedTopic).increment();
            log.error("Failed to send OrderStatusChangedEvent for orderId={}: {}",
                event.getOrderId(), ex.getMessage());
          } else {
            meterRegistry.counter("kafka.producer.success",
                "topic", ordersStatusChangedTopic).increment();
            log.debug("Sent status change to partition={}, offset={}",
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}
