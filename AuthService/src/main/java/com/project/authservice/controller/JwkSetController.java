package com.project.authservice.controller;

import com.project.authservice.config.SecurityConfig;
import com.project.authservice.security.JwtKeyManager;
import com.project.authservice.security.JwtProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What makes the other services independent of this one at request time: they fetch the public
 * key once, cache it, and verify every token locally. AuthService being down stops logins, not
 * traffic.
 */
@RestController
@RequiredArgsConstructor
public class JwkSetController {

  @Value("${security.jwt.issuer}")
  private String issuer;

  private final JwtKeyManager keyManager;

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> jwks() {
    return keyManager.publicJwkSet();
  }

  /**
   * Discovery, so a Spring resource server can be configured with nothing but
   * {@code issuer-uri}. Both paths are served because Spring probes the OpenID one and the plain
   * OAuth2 one in turn.
   */
  @GetMapping({"/.well-known/openid-configuration", "/.well-known/oauth-authorization-server"})
  public Map<String, Object> metadata() {
    return Map.of(
        "issuer", issuer,
        "jwks_uri", issuer + "/.well-known/jwks.json",
        "token_endpoint", issuer + "/oauth2/token",
        "introspection_endpoint", issuer + "/api/v1/auth/introspect",
        "grant_types_supported", List.of("client_credentials"),
        "token_endpoint_auth_methods_supported",
        List.of("client_secret_basic", "client_secret_post"),
        "introspection_endpoint_auth_methods_supported", List.of("bearer"),
        "id_token_signing_alg_values_supported", List.of("RS256"),
        "subject_types_supported", List.of("public"),
        "scopes_supported", List.of(SecurityConfig.INTROSPECT_SCOPE));
  }
}
