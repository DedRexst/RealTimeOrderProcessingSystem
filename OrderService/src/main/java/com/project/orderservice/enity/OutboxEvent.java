package com.project.orderservice.enity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_event", indexes = @Index(name = "idx_outbox_event_published_at",
    columnList = "published_at"))
public class OutboxEvent extends BaseEntity {

  private static final int MAX_ERROR_LENGTH = 1000;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 64)
  private OutboxEventType eventType;

  /** The Kafka key, so the relay keeps every event of one aggregate on one partition. */
  @Column(name = "aggregate_id", nullable = false, length = 64)
  private String aggregateId;

  @Column(name = "payload", nullable = false, columnDefinition = "text")
  private String payload;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "last_error", length = MAX_ERROR_LENGTH)
  private String lastError;

  public OutboxEvent(OutboxEventType eventType, String aggregateId, String payload) {
    this.eventType = eventType;
    this.aggregateId = aggregateId;
    this.payload = payload;
  }

  public void markPublished() {
    this.attempts++;
    this.publishedAt = Instant.now();
    this.lastError = null;
  }

  public void recordFailure(Throwable error) {
    this.attempts++;
    String message = String.valueOf(error);
    this.lastError = message.length() > MAX_ERROR_LENGTH
        ? message.substring(0, MAX_ERROR_LENGTH) : message;
  }
}
