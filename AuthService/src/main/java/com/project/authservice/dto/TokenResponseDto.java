package com.project.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 6749 §5.1 shape, used by both the login and the {@code client_credentials} responses so a
 * generic OAuth2 client library can consume either one.
 *
 * <p>{@code refreshToken} is null for a client token: a machine holding its own secret can always
 * mint another, and a long-lived refresh token would only be one more thing to leak.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponseDto(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    long expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken,

    String scope
) {
  public static TokenResponseDto bearer(String accessToken, long expiresIn, String refreshToken,
                                        String scope) {
    return new TokenResponseDto(
        accessToken,
        "Bearer",
        expiresIn,
        refreshToken,
        scope);
  }
}
