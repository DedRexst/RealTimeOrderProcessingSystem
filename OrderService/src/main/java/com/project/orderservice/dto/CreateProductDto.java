package com.project.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * A new catalogue entry. {@code initialStock} is not stored here: OrderService does not track stock,
 * it only hands the number to InventoryService in the {@code products.created} event so the product
 * can be ordered as soon as it exists.
 */
public record CreateProductDto(
    @NotBlank
    @Size(max = 255)
    String name,
    @Size(max = 255)
    String description,
    @NotNull
    @PositiveOrZero
    Long initialStock
) {}
