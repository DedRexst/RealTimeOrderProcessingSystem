package com.project.analyticsservice.listener;

import com.project.analyticsservice.event.OrderStatusChangedEvent;
import com.project.analyticsservice.mapper.OrderStatusChangedMapper;
import com.project.analyticsservice.service.OrderStatusAnalyticsService;
import com.project.analyticsservice.service.StatsPublisher;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedListener {

  private static final Duration RETRY_DELAY = Duration.ofSeconds(10);

  private final OrderStatusAnalyticsService analyticsService;
  private final StatsPublisher statsPublisher;
  private final OrderStatusChangedMapper mapper;

  @KafkaListener(topics = "${kafka.topics.orders-status-changed}")
  public void handleOrderStatusChanged(
      @Payload OrderStatusChangedEvent event,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment acknowledgment) {

    log.info("Received OrderStatusChangedEvent: orderId={}, {} -> {}, partition={}, offset={}",
        event.getOrderId(), event.getPreviousStatus(), event.getNewStatus(), partition, offset);

    if (event.getOrderId() == null || event.getNewStatus() == null) {
      log.error("Discarding status change without an orderId or newStatus at partition={},"
          + " offset={}", partition, offset);
      acknowledgment.acknowledge();
      return;
    }

    recordAnalytics(event);

    try {
      statsPublisher.publishOrderStatusChanged(mapper.eventToDto(event));
      acknowledgment.acknowledge();

    } catch (HttpClientErrorException e) {
      log.error("Webhook rejected the status change for orderId={} with {}, discarding",
          event.getOrderId(), e.getStatusCode(), e);
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to relay status change for orderId={}, will retry",
          event.getOrderId(), e);
      acknowledgment.nack(RETRY_DELAY);
    }
  }

  private void recordAnalytics(OrderStatusChangedEvent event) {
    try {
      analyticsService.record(event).ifPresentOrElse(
          statsPublisher::publishStatusAnalytics,
          () -> log.debug("Status change for orderId={} already counted", event.getOrderId()));
    } catch (Exception e) {
      log.error("Analytics failed for the status change of orderId={}, relaying it anyway",
          event.getOrderId(), e);
    }
  }
}
