package com.project.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * RFC 7662 §2.2. Everything but {@code active} is omitted when the token is not valid — telling a
 * caller why a token failed is telling an attacker which half of it to fix.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IntrospectionResponseDto(
    boolean active,
    String sub,
    String scope,

    @JsonProperty("client_id")
    String clientId,

    /** {@code user} or {@code client}: which of the two kinds of caller is behind the token. */
    @JsonProperty("token_use")
    String tokenUse,

    String email,
    List<String> roles,
    Long exp,
    Long iat,
    String iss,
    String jti
) {

  public static final IntrospectionResponseDto INACTIVE =
      new IntrospectionResponseDto(false, null, null, null, null, null, null, null, null, null,
          null);
}
