package com.project.authservice.security;

/** The claim names shared with every service that verifies a token issued here. */
public final class JwtClaims {

  /** {@code user} or {@code client} — whether a person or a registered system is calling. */
  public static final String TOKEN_USE = "token_use";
  public static final String TOKEN_USE_USER = "user";
  public static final String TOKEN_USE_CLIENT = "client";

  public static final String ROLES = "roles";
  public static final String SCOPE = "scope";
  public static final String EMAIL = "email";
  public static final String CLIENT_ID = "client_id";

  private JwtClaims() {
  }
}
