package com.project.authservice.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

  /** Written to `iss` and checked on every token this service verifies. */
  @NotBlank
  private String issuer = "http://localhost:8085";

  @NotNull
  private Duration accessTokenTtl = Duration.ofMinutes(15);

  @NotNull
  private Duration refreshTokenTtl = Duration.ofDays(30);

  @NotNull
  private Duration clientTokenTtl = Duration.ofMinutes(10);

  /** Directory holding the RSA keypair as PEM; created with the keypair on first start. */
  @NotBlank
  private String keyDirectory = "./data/auth-keys";
}
