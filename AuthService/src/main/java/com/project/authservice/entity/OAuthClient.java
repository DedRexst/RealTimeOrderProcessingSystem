package com.project.authservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A third-party system registered against this platform: a partner integration, a fulfilment
 * provider, or one of our own services calling another. It authenticates as itself with the
 * {@code client_credentials} grant — there is no user behind its calls, which is exactly why its
 * token carries scopes and no roles.
 */
@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "oauth_clients", uniqueConstraints = @UniqueConstraint(
    name = "uk_oauth_clients_client_id", columnNames = "client_id"))
public class OAuthClient extends BaseEntity {

  @Column(name = "client_id", nullable = false, length = 64)
  private String clientId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  /** Who to reach when this integration starts misbehaving or its secret has to be rotated. */
  @Column(name = "contact_email")
  private String contactEmail;

  /** BCrypt, like a user's password: a leaked backup must not yield callable credentials. */
  @ToString.Exclude
  @Column(name = "secret_hash", nullable = false)
  private String secretHash;

  /**
   * The ceiling on what this client may ask for. A token request may narrow this set but never
   * widen it, so revoking a permission here takes effect on the client's next token.
   */
  @Builder.Default
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth_client_scopes",
      joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "scope", nullable = false)
  private Set<String> scopes = new HashSet<>();

  /**
   * Kill switch for an integration: disabling it stops new tokens immediately and makes every
   * introspection of an already-issued one answer {@code active: false}.
   */
  @Builder.Default
  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Column(name = "secret_rotated_at")
  private Instant secretRotatedAt;
}
