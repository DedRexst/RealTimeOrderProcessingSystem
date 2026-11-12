package com.project.orderservice.dto;

import com.project.orderservice.enity.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderDto(
    @NotNull
    Long customerId,
    @NotEmpty
    @Size(max = 100)
    List<@NotNull Long> productIds,
    @NotNull
    Currency currency,
    @NotBlank
    String address
) {}
