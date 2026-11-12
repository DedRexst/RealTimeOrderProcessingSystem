package com.project.orderservice.listener;

import com.project.orderservice.event.InventoryReservedEvent;
import com.project.orderservice.producer.OrderStatusChangePublisher;
import com.project.orderservice.service.order.OrderService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Closes the order saga: InventoryService answers OrderCreatedEvent with InventoryReservedEvent,
 * and that answer is what moves the order out of PENDING.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final OrderService orderService;
  private final OrderStatusChangePublisher orderStatusChangePublisher;

  @KafkaListener(topics = "${kafka.topics.inventory-reserved}")
  public void handleInventoryReserved(
      @Payload InventoryReservedEvent event,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment acknowledgment) {

    log.info("Received InventoryReservedEvent: orderId={}, partition={}, offset={}",
        event.getOrderId(), partition, offset);

    if (event.getOrderId() == null) {
      log.error("Discarding InventoryReservedEvent without an orderId at partition={}, offset={}",
          partition, offset);
      acknowledgment.acknowledge();
      return;
    }

    try {
      orderService.markInventoryReserved(event.getOrderId())
          .ifPresent(orderStatusChangePublisher::publish);

      acknowledgment.acknowledge();

    } catch (EntityNotFoundException e) {
      log.error("No order found for orderId={}, discarding event", event.getOrderId(), e);
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Transient error updating orderId={}, will retry", event.getOrderId(), e);
      acknowledgment.nack(Duration.ofSeconds(5));
    }
  }
}
