package com.project.inventoryservice.listener;

import com.project.inventoryservice.event.ProductCreatedEvent;
import com.project.inventoryservice.service.InventoryService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Creates the stock row for each product OrderService adds, so the product can be reserved.
 * OrderService's outbox delivers at-least-once; a repeated event finds the row already there and
 * changes nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

  private final InventoryService inventoryService;

  @KafkaListener(topics = "${kafka.topics.products-created}",
      containerFactory = "productCreatedListenerContainerFactory")
  public void handleProductCreated(@Payload ProductCreatedEvent event,
      Acknowledgment acknowledgment) {

    if (event.getProductId() == null) {
      log.warn("Ignoring ProductCreatedEvent without a productId");
      acknowledgment.acknowledge();
      return;
    }

    try {
      boolean created = inventoryService.registerProduct(event.getProductId(),
          event.getInitialStock() == null ? 0L : event.getInitialStock());
      if (!created) {
        log.info("Product {} already stocked, ignoring repeated event", event.getProductId());
      }
      acknowledgment.acknowledge();

    } catch (DataIntegrityViolationException e) {
      log.info("Product {} was created concurrently, treating as already processed",
          event.getProductId());
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Transient error registering product {}, will retry", event.getProductId(), e);
      acknowledgment.nack(Duration.ofSeconds(5));
    }
  }
}
