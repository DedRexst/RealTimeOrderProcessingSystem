package com.project.authservice.repository;

import com.project.authservice.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * The reaction to a replayed token: one leaked refresh token invalidates the whole session
   * family, because at that point we cannot tell the thief's next request from the owner's.
   */
  @Modifying
  @Query("update RefreshToken t set t.revoked = true, t.revokedAt = :now "
      + "where t.user.id = :userId and t.revoked = false")
  int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);

  @Modifying
  @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
  int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
