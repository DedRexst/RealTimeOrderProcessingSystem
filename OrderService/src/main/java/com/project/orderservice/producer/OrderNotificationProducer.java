package com.project.orderservice.producer;

import com.project.orderservice.event.OrderNotificationEvent;
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
 * Publishes customer-facing order updates for NotificationService to turn into email.
 *
 * <p>Keyed by orderId so every update for one order lands on the same partition and the emails
 * cannot overtake each other — a customer must not read "reserved" after "failed".
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNotificationProducer {

  @Value("${kafka.topics.orders-notifications}")
  private String ordersNotificationsTopic;

  private final KafkaTemplate<String, OrderNotificationEvent> kafkaTemplate;
  private final MeterRegistry meterRegistry;

  @Transactional("kafkaTransactionManager")
  public CompletableFuture<SendResult<String, OrderNotificationEvent>> publishOrderNotification(
      OrderNotificationEvent event) {

    log.info("Publishing OrderNotificationEvent: orderId={}, status={}, topic={}",
        event.getOrderId(), event.getStatus(), ordersNotificationsTopic);

    return kafkaTemplate
        .send(ordersNotificationsTopic, event.getOrderId().toString(), event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            meterRegistry.counter("kafka.producer.errors",
                "topic", ordersNotificationsTopic).increment();
            log.error("Failed to send OrderNotificationEvent for orderId={}: {}",
                event.getOrderId(), ex.getMessage());
          } else {
            meterRegistry.counter("kafka.producer.success",
                "topic", ordersNotificationsTopic).increment();
            log.debug("Sent notification to partition={}, offset={}",
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}
