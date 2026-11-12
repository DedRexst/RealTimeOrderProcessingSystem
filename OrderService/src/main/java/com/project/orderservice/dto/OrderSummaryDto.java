package com.project.orderservice.dto;

import com.project.orderservice.enity.Currency;
import com.project.orderservice.enity.Status;
import java.time.Instant;
import java.util.List;

/**
 * One row of the order list. A superset of {@link OrderResponseDto} rather than the same record:
 * a list needs the customer's name to be readable and the timestamps to be sortable, and neither
 * belongs in the response to {@code POST /orders}, which answers about an order the caller has
 * just described in full.
 */
public record OrderSummaryDto(
    Long id,
    Long customerId,
    String customerName,
    List<Long> productIds,
    Currency currency,
    String address,
    Status status,
    Instant createdTime,
    Instant updatedTime
) {}
