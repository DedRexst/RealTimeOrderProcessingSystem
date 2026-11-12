package com.project.notificationservice.listener;

import com.project.notificationservice.event.OrderNotificationEvent;
import com.project.notificationservice.service.OrderEmailService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

  private final OrderEmailService orderEmailService;

  @KafkaListener(topics = "${kafka.topics.orders-notifications}")
  public void handleOrderNotification(
      @Payload OrderNotificationEvent event,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment acknowledgment) {

    log.info("Received OrderNotificationEvent: orderId={}, status={}, partition={}, offset={}",
        event.getOrderId(), event.getStatus(), partition, offset);

    if (event.getOrderId() == null || isBlank(event.getCustomerEmail())) {
      log.error("Discarding notification without an orderId or customer email at partition={},"
          + " offset={}", partition, offset);
      acknowledgment.acknowledge();
      return;
    }

    try {
      orderEmailService.sendOrderStatusEmail(event);
      acknowledgment.acknowledge();

    } catch (MailParseException e) {
      // Permanent: a malformed address or subject will fail identically on every redelivery.
      log.error("Cannot compose email for orderId={}, discarding", event.getOrderId(), e);
      acknowledgment.acknowledge();

    } catch (MailException e) {
      // Transient: SMTP unreachable, greylisting, mailbox temporarily unavailable.
      log.error("SMTP failure for orderId={}, will retry", event.getOrderId(), e);
      acknowledgment.nack(Duration.ofSeconds(10));

    } catch (Exception e) {
      log.error("Unexpected failure handling notification for orderId={}, will retry",
          event.getOrderId(), e);
      acknowledgment.nack(Duration.ofSeconds(10));
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
