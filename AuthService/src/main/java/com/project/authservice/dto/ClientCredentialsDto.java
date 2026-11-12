package com.project.authservice.dto;

/**
 * The one and only response that carries a client secret in the clear. Only the BCrypt hash is
 * kept, so a lost secret is replaced by rotation, never recovered — which the {@code note} says
 * out loud to whoever is reading the response in a terminal.
 */
public record ClientCredentialsDto(
    ClientDto client,
    String clientSecret,
    String note
) {}
