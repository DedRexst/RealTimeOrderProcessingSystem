package com.project.orderservice.dto;

import com.project.orderservice.enity.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * What an operator or an external order-tracking service sends to move an order on.
 *
 * <p>{@code reason} and {@code changedBy} are optional but travel all the way to the customer email
 * and the analytics webhook, so a caller that fills them in explains the change instead of leaving
 * a bare status behind. Both are length-capped: they end up in an email body and in an outbound
 * HTTP payload, neither of which should be sizeable at the caller's discretion.
 */
public record UpdateOrderStatusDto(
    @NotNull
    Status status,

    @Size(max = 500)
    String reason,

    /** Who asked for the change — an operator login, or the name of the calling service. */
    @Size(max = 120)
    String changedBy
) {}
