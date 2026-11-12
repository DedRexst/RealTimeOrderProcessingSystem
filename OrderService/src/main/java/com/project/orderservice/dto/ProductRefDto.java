package com.project.orderservice.dto;

/** The catalogue as a form needs it: enough to name a product in a dropdown, and no more. */
public record ProductRefDto(
    Long id,
    String name,
    String description
) {}
