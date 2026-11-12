package com.project.authservice.service.token.impl;

import com.project.authservice.entity.OAuthClient;
import com.project.authservice.entity.RefreshToken;
import com.project.authservice.entity.User;
import com.project.authservice.repository.RefreshTokenRepository;
import com.project.authservice.util.SecretsUtil;
import com.project.authservice.service.token.TokenService;
import com.project.authservice.service.token.mapper.TokenServiceMapper;
import com.project.authservice.util.exception.BusinessException;
import com.project.authservice.util.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

  private static final int REFRESH_TOKEN_BYTES = 32;

  @Value("${security.jwt.access-token-ttl}")
  private Duration accessTokenTtl;
  @Value("${security.jwt.refresh-token-ttl}")
  private Duration refreshTokenTtl;
  @Value("${security.jwt.issuer}")
  private String issuer;

  private final TokenServiceMapper tokenServiceMapper;
  private final JwtDecoder jwtDecoder;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public String generateRefreshToken(User user, Instant instant) {
    String rawRefreshToken = SecretsUtil.randomToken(REFRESH_TOKEN_BYTES);
    RefreshToken refreshToken = tokenServiceMapper.mapRefreshToken(user, rawRefreshToken, instant, refreshTokenTtl);
    refreshTokenRepository.save(refreshToken);
    return rawRefreshToken;
  }

  @Override
  public String generateAccessToken(User user, Instant instant) {
    Instant expiresAt = instant.plus(accessTokenTtl);
    String accessToken = tokenServiceMapper.mapAccessTokenForUser(user, instant, expiresAt, issuer);
    return accessToken;
  }

  @Override
  public String getAccessTokenForClient(OAuthClient client, String effectiveScopes, Duration clientTokenTtl) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(clientTokenTtl);

    return tokenServiceMapper.mapAccessTokenForClient(client, effectiveScopes, now, expiresAt, issuer);
  }

  @Override
  public void isRefreshTokenValid(RefreshToken refreshToken, Instant now) {
    if (refreshToken.isRevoked()) {
      int revoked = refreshTokenRepository.revokeAllForUser(refreshToken.getUser().getId(), now);
      log.warn("Replay of a used refresh token for user {}; revoked {} remaining session(s)",
          refreshToken.getUser().getId(), revoked);
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_ALREADY_USED);
    }
    if (!refreshToken.isUsable(now)) {
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public RefreshToken getRefreshToken(String rawRefreshToken) {
    return refreshTokenRepository.findByTokenHash(SecretsUtil.sha256Hex(rawRefreshToken))
        .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_RECOGNISED));
  }

  @Override
  public void revokeRefreshToken(String rawRefreshToken) {
    refreshTokenRepository.findByTokenHash(SecretsUtil.sha256Hex(rawRefreshToken))
        .filter(token -> !token.isRevoked())
        .ifPresent(token -> {
          token.setRevoked(true);
          token.setRevokedAt(Instant.now());
        });
  }

  @Override
  public Optional<Jwt> decodeAccessToken(String accessToken) {
    try {
      return Optional.of(jwtDecoder.decode(accessToken));
    } catch (JwtException e) {
      log.debug("Rejected an access token: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
