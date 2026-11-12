package com.project.orderservice.dto;

import com.project.orderservice.enity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * What a registered third-party system — the carrier, the warehouse, the payment provider — reports
 * about an order it is handling on our behalf. Deliberately not {@link UpdateOrderStatusDto}: the
 * two callers are told apart by more than their token.
 *
 * <p>There is no {@code changedBy} here. A machine caller is named by the token it presented, so a
 * compromised integration cannot sign a transition as an operator; see
 * {@code OrderFacadeImpl#reporterOf}.
 *
 * <p>{@code externalReference} is mandatory, unlike everything else the caller may say: it is the
 * id of the record on the other side — a shipment, a payment, a ticket — and without it a
 * transition that later turns out to be wrong cannot be traced back to what caused it.
 *
 * @param occurredAt when the change happened in the caller's system, which for a batched or retried
 *     callback is meaningfully earlier than its arrival here. Optional; the arrival time stands in
 *     when it is absent, and a timestamp from the future is not trusted over our own clock.
 */
public record ExternalStatusUpdateDto(
    @NotNull
    Status status,

    @Size(max = 500)
    String reason,

    @NotBlank
    @Size(max = 120)
    String externalReference,

    Instant occurredAt
) {}
