package com.project.orderservice.dto;

/**
 * A customer as the order form needs them — the address is here because it is what the form
 * prefills the delivery address from.
 *
 * <p>Email and phone are left out on purpose. Any authenticated caller can read this list, and a
 * dropdown has no use for contact details that a leak would make personal data.
 */
public record CustomerRefDto(
    Long id,
    String name,
    String address
) {}
