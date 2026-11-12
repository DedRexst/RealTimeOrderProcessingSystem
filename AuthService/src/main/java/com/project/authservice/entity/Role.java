package com.project.authservice.entity;

/**
 * What a human account is allowed to do. Rendered into the {@code roles} claim of an access token
 * and turned back into a {@code ROLE_*} authority by every service that verifies the token, so the
 * names here are part of the contract with the rest of the system, not an internal detail.
 */
public enum Role {
  USER,
  ADMIN
}
