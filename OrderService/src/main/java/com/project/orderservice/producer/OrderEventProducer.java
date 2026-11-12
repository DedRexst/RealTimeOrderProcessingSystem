package com.project.orderservice.producer;

import com.project.orderservice.event.OrderCreatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

  @Value("${kafka.topics.orders-created}")
  private String ordersCreatedTopic;

  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
  private final MeterRegistry meterRegistry;

  @Transactional("kafkaTransactionManager")
  public CompletableFuture<SendResult<String, OrderCreatedEvent>> publishOrderCreated(OrderCreatedEvent event) {

    log.info("Publishing OrderCreatedEvent: orderId={}, ordersCreatedTopic={}", event.getOrderId(), ordersCreatedTopic);

    return kafkaTemplate
        .send(ordersCreatedTopic, event.getOrderId().toString(), event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            meterRegistry.counter("kafka.producer.errors",
                "topic", ordersCreatedTopic).increment();
            log.error("Failed to send event: {}", ex.getMessage());
          } else {
            meterRegistry.counter("kafka.producer.success",
                "topic", ordersCreatedTopic).increment();
            log.debug("Sent to partition={}, offset={}",
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}
