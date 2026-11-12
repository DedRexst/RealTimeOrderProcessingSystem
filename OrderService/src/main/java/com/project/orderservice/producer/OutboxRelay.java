package com.project.orderservice.producer;

import com.project.orderservice.enity.OutboxEvent;
import com.project.orderservice.event.OrderCreatedEvent;
import com.project.orderservice.event.OrderNotificationEvent;
import com.project.orderservice.event.ProductCreatedEvent;
import com.project.orderservice.reporsitory.OutboxEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes committed outbox rows to Kafka.
 *
 * <p>A row is marked published only after Kafka has acknowledged it. If this service stops between
 * the acknowledgement and that mark, the row is sent again on the next poll — delivery is
 * at-least-once, so consumers must tolerate a repeated event for the same order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

  private final OutboxEventRepository outboxEventRepository;
  private final OrderEventProducer orderEventProducer;
  private final OrderNotificationProducer orderNotificationProducer;
  private final ProductEventProducer productEventProducer;
  private final JsonMapper jsonMapper;

  @Value("${outbox.batch-size:100}")
  private int batchSize;

  @Value("${outbox.send-timeout-ms:10000}")
  private long sendTimeoutMs;

  @Value("${outbox.retention-days:7}")
  private int retentionDays;

  @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
  @Transactional("transactionManager")
  public void relay() {
    List<OutboxEvent> batch = outboxEventRepository.lockUnpublished(batchSize);
    for (OutboxEvent event : batch) {
      try {
        send(event);
        event.markPublished();
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        Throwable cause = e instanceof ExecutionException && e.getCause() != null
            ? e.getCause() : e;
        event.recordFailure(cause);
        log.warn("Outbox event {} ({} for {}) not published, attempt {}; retrying on next poll",
            event.getId(), event.getEventType(), event.getAggregateId(), event.getAttempts(),
            cause);
        // Kafka is most likely unreachable, and every further send would wait out the same
        // timeout. The rest of the batch is left for the next poll.
        break;
      }
    }
  }

  @Scheduled(cron = "${outbox.cleanup-cron:0 0 3 * * *}")
  @Transactional("transactionManager")
  public void purgePublished() {
    int deleted = outboxEventRepository.deletePublishedBefore(
        Instant.now().minus(Duration.ofDays(retentionDays)));
    if (deleted > 0) {
      log.info("Purged {} published outbox events older than {} days", deleted, retentionDays);
    }
  }

  private void send(OutboxEvent event) throws Exception {
    switch (event.getEventType()) {
      case ORDER_CREATED -> orderEventProducer
          .publishOrderCreated(jsonMapper.readValue(event.getPayload(), OrderCreatedEvent.class))
          .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
      case ORDER_NOTIFICATION -> orderNotificationProducer
          .publishOrderNotification(
              jsonMapper.readValue(event.getPayload(), OrderNotificationEvent.class))
          .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
      case PRODUCT_CREATED -> productEventProducer
          .publishProductCreated(
              jsonMapper.readValue(event.getPayload(), ProductCreatedEvent.class))
          .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
    }
  }
}
