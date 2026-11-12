package com.project.orderservice.producer;

import com.project.orderservice.event.ProductCreatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Announces new catalogue products to InventoryService, keyed by productId. */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEventProducer {

  @Value("${kafka.topics.products-created}")
  private String productsCreatedTopic;

  private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
  private final MeterRegistry meterRegistry;

  @Transactional("kafkaTransactionManager")
  public CompletableFuture<SendResult<String, ProductCreatedEvent>> publishProductCreated(
      ProductCreatedEvent event) {

    log.info("Publishing ProductCreatedEvent: productId={}, topic={}",
        event.getProductId(), productsCreatedTopic);

    return kafkaTemplate
        .send(productsCreatedTopic, event.getProductId().toString(), event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            meterRegistry.counter("kafka.producer.errors",
                "topic", productsCreatedTopic).increment();
            log.error("Failed to send ProductCreatedEvent for productId={}: {}",
                event.getProductId(), ex.getMessage());
          } else {
            meterRegistry.counter("kafka.producer.success",
                "topic", productsCreatedTopic).increment();
          }
        });
  }
}
