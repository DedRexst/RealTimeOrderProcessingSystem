package com.project.authservice.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * A failure of the {@code /oauth2/token} endpoint, which answers in RFC 6749 §5.2 terms rather
 * than with a ProblemDetail: the callers are generic OAuth2 client libraries, and they branch on
 * the {@code error} code.
 */
@Getter
public class OAuth2Exception extends RuntimeException {

  private final String error;
  private final HttpStatus status;

  public OAuth2Exception(String error, HttpStatus status, String description) {
    super(description);
    this.error = error;
    this.status = status;
  }

  public static OAuth2Exception invalidClient(String description) {
    return new OAuth2Exception("invalid_client", HttpStatus.UNAUTHORIZED, description);
  }

  public static OAuth2Exception invalidRequest(String description) {
    return new OAuth2Exception("invalid_request", HttpStatus.BAD_REQUEST, description);
  }

  public static OAuth2Exception invalidScope(String description) {
    return new OAuth2Exception("invalid_scope", HttpStatus.BAD_REQUEST, description);
  }

  public static OAuth2Exception unsupportedGrantType(String grantType) {
    return new OAuth2Exception("unsupported_grant_type", HttpStatus.BAD_REQUEST,
        "Unsupported grant_type: " + grantType);
  }
}
