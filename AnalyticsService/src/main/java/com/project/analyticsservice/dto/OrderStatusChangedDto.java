package com.project.analyticsservice.dto;

import java.time.Instant;
import java.util.List;

/**
 * Webhook body for one order status transition. Kept separate from the inbound event so the
 * outward contract does not change when the internal Kafka payload does.
 */
public record OrderStatusChangedDto(
    Long orderId,
    Long customerId,
    String previousStatus,
    String newStatus,
    String currency,
    List<Long> productIds,
    String reason,
    String changedBy,
    String externalReference,
    Instant occurredAt
) {}
