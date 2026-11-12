package com.project.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * The only revocable half of a session. Access tokens are self-contained and nobody asks this
 * service before trusting one, so ending a session means refusing to mint the next access token.
 *
 * <p>The raw token never reaches this table — only its SHA-256 — so a dump of the database does
 * not hand over working sessions.
 */
@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "refresh_tokens", uniqueConstraints = @UniqueConstraint(
    name = "uk_refresh_tokens_hash", columnNames = "token_hash"))
public class RefreshToken extends BaseEntity {

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  private boolean revoked;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  public boolean isUsable(Instant now) {
    return !revoked && expiresAt.isAfter(now);
  }
}
