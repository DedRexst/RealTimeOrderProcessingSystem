package com.project.notificationservice.service;

import com.project.notificationservice.event.OrderNotificationEvent;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Renders an order-status event as a plain-text email and hands it to SMTP. */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

  private final JavaMailSender mailSender;

  @Value("${notification.mail.from}")
  private String from;

  public void sendOrderStatusEmail(OrderNotificationEvent event) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(event.getCustomerEmail());
    message.setSubject(subjectFor(event));
    message.setText(bodyFor(event));

    mailSender.send(message);

    log.info("Sent {} email for orderId={} to {}",
        event.getStatus(), event.getOrderId(), event.getCustomerEmail());
  }

  private String subjectFor(OrderNotificationEvent event) {
    return "Order #%s — %s".formatted(event.getOrderId(), headlineFor(event.getStatus()));
  }

  private String headlineFor(String status) {
    if (status == null) {
      return "status updated";
    }
    return switch (status) {
      case "PENDING" -> "received, awaiting stock";
      case "RESERVED" -> "items reserved";
      case "SUCCESSFUL" -> "confirmed";
      case "RELEASED" -> "released";
      case "FAILED" -> "could not be completed";
      default -> "status updated to " + status;
    };
  }

  private String sentenceFor(String status) {
    if (status == null) {
      return "Your order #%s has been updated.";
    }
    return switch (status) {
      case "PENDING" -> "We have received your order #%s and are reserving the items now.";
      case "RESERVED" -> "The items in your order #%s are reserved and being prepared.";
      case "SUCCESSFUL" -> "Your order #%s is confirmed.";
      case "RELEASED" -> "The items reserved for your order #%s have been released.";
      case "FAILED" -> "Unfortunately your order #%s could not be completed.";
      default -> "Your order #%s is now " + status + ".";
    };
  }

  private String statusLineFor(OrderNotificationEvent event) {
    if (isBlank(event.getPreviousStatus())) {
      return String.valueOf(event.getStatus());
    }
    return "%s (was %s)".formatted(event.getStatus(), event.getPreviousStatus());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String bodyFor(OrderNotificationEvent event) {
    StringBuilder body = new StringBuilder();

    body.append("Hello %s,%n%n".formatted(
        Objects.requireNonNullElse(event.getCustomerName(), "there")));
    body.append(sentenceFor(event.getStatus()).formatted(event.getOrderId()));
    body.append("%n".formatted());

    if (!isBlank(event.getReason())) {
      body.append("%nReason: %s%n".formatted(event.getReason()));
    }
    body.append("%n".formatted());

    body.append("Order details%n".formatted());
    body.append("-------------%n".formatted());
    body.append("Status:    %s%n".formatted(statusLineFor(event)));
    body.append("Currency:  %s%n".formatted(
        Objects.requireNonNullElse(event.getCurrency(), "-")));
    body.append("Ships to:  %s%n".formatted(
        Objects.requireNonNullElse(event.getAddress(), "-")));
    if (event.getOccurredAt() != null) {
      body.append("Updated:   %s%n".formatted(event.getOccurredAt()));
    }

    if (!isBlank(event.getExternalReference())) {
      body.append("Reference: %s%n".formatted(event.getExternalReference()));
    }

    body.append("%nItems%n".formatted());
    body.append("-----%n".formatted());
    List<OrderNotificationEvent.ProductLine> products = event.getProducts();
    if (products == null || products.isEmpty()) {
      body.append("(no items listed)%n".formatted());
    } else {
      products.forEach(product -> body.append("  - %s (#%s)%n".formatted(
          Objects.requireNonNullElse(product.getName(), "unnamed product"), product.getId())));
    }

    body.append("%nThank you for shopping with us.%n".formatted());
    return body.toString();
  }
}
