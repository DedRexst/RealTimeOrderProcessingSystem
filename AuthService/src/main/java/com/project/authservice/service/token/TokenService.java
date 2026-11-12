package com.project.authservice.service.token;

import com.project.authservice.entity.OAuthClient;
import com.project.authservice.entity.RefreshToken;
import com.project.authservice.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;

/** Everything that mints, rotates, or judges a token. */
public interface TokenService {

  String generateRefreshToken(User user, Instant instant);

  String generateAccessToken(User user, Instant instant);

  /**
   * Access token only: a client re-authenticates with its secret instead of refreshing.
   */
  String getAccessTokenForClient(OAuthClient client, String effectiveScopes, Duration clientTokenTtl);

  void isRefreshTokenValid(RefreshToken refreshToken, Instant now);

  RefreshToken getRefreshToken(String rawRefreshToken);

  /** Ends one session. Unknown or already-revoked tokens are accepted silently. */
  void revokeRefreshToken(String rawRefreshToken);

  /**
   * Checks signature, expiry, and issuer. Empty for any token failing them; the state of the
   * account or client behind a token that passes is the caller's to judge.
   */
  Optional<Jwt> decodeAccessToken(String accessToken);
}
