package com.project.orderservice.service.outbox;

import com.project.orderservice.enity.OutboxEvent;
import com.project.orderservice.enity.OutboxEventType;
import com.project.orderservice.event.OrderCreatedEvent;
import com.project.orderservice.event.OrderNotificationEvent;
import com.project.orderservice.event.ProductCreatedEvent;
import com.project.orderservice.reporsitory.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxEventRepository outboxEventRepository;
  private final JsonMapper jsonMapper;

 @Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
  public void enqueueOrderCreated(OrderCreatedEvent event) {
    outboxEventRepository.save(new OutboxEvent(
        OutboxEventType.ORDER_CREATED,
        event.getOrderId().toString(),
        jsonMapper.writeValueAsString(event)));
  }

  @Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
  public void enqueueOrderNotification(OrderNotificationEvent event) {
    outboxEventRepository.save(new OutboxEvent(
        OutboxEventType.ORDER_NOTIFICATION,
        event.getOrderId().toString(),
        jsonMapper.writeValueAsString(event)));
  }

  @Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
  public void enqueueProductCreated(ProductCreatedEvent event) {
    outboxEventRepository.save(new OutboxEvent(
        OutboxEventType.PRODUCT_CREATED,
        event.getProductId().toString(),
        jsonMapper.writeValueAsString(event)));
  }
}
