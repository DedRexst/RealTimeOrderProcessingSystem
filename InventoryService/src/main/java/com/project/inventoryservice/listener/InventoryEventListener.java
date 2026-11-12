package com.project.inventoryservice.listener;

import com.project.inventoryservice.event.InventoryReservedEvent;
import com.project.inventoryservice.event.OrderCreatedEvent;
import com.project.inventoryservice.producer.InventoryEventProducer;
import com.project.inventoryservice.service.InventoryService;
import com.project.inventoryservice.util.BusinessException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

  private final InventoryService inventoryService;
  private final InventoryEventProducer eventProducer;

  @KafkaListener(topics = "${kafka.topics.orders-created}")
  public void handleOrderCreated(
      @Payload OrderCreatedEvent event,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment acknowledgment) {

    log.info("Received: orderId={}, partition={}, offset={}",
        event.getOrderId(), partition, offset);

    InventoryReservedEvent reservedEvent =
        new InventoryReservedEvent(event.getOrderId(), event.getProductId());

    try {
      inventoryService.reserve(reservedEvent);
      eventProducer.publishInventoryReserved(reservedEvent);
      acknowledgment.acknowledge();

    } catch (DataIntegrityViolationException e) {

      log.warn("Duplicate reservation for orderId={} rejected by the database, treating as already"
          + " processed", event.getOrderId());
      acknowledgment.acknowledge();

    } catch (BusinessException e) {
      log.warn("Cannot reserve inventory for orderId={}: {}", event.getOrderId(), e.getMessage());
      eventProducer.publishToDlq(reservedEvent, e.getMessage());
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Transient error reserving inventory for orderId={}, will retry",
          event.getOrderId(), e);
      acknowledgment.nack(Duration.ofSeconds(5));
    }
  }
}
