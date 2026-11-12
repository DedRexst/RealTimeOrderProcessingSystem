package com.project.orderservice.dto;

import com.project.orderservice.enity.Currency;
import com.project.orderservice.enity.Status;
import java.util.List;

public record OrderResponseDto(
    Long id,
    Long customerId,
    List<Long> productIds,
    Currency currency,
    String address,
    Status status
) {}
