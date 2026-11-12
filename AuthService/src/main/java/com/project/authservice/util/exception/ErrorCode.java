package com.project.authservice.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Every business failure the service can report, answered as a ProblemDetail. */
@Getter
public enum ErrorCode {

  EMAIL_ALREADY_USED(HttpStatus.CONFLICT, "Email already registered: %s"),

  INVALID_EMAIL(HttpStatus.UNAUTHORIZED, "Invalid email"),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password"),
  USER_DELETED(HttpStatus.UNAUTHORIZED, "User with this email are deleted"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found: %s"),


  REFRESH_TOKEN_NOT_RECOGNISED(HttpStatus.UNAUTHORIZED, "Refresh token is not recognised"),
  REFRESH_TOKEN_ALREADY_USED(HttpStatus.UNAUTHORIZED, "Refresh token has already been used"),
  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token has expired"),
  ACCOUNT_INACTIVE(HttpStatus.UNAUTHORIZED, "Account is no longer active");

  private final HttpStatus status;
  private final String messageTemplate;

  ErrorCode(HttpStatus status, String messageTemplate) {
    this.status = status;
    this.messageTemplate = messageTemplate;
  }
}
