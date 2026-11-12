package com.project.authservice.service.token.mapper;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.authservice.entity.OAuthClient;
import com.project.authservice.entity.RefreshToken;
import com.project.authservice.entity.Role;
import com.project.authservice.entity.User;
import com.project.authservice.security.JwtClaims;
import com.project.authservice.security.JwtKeyManager;
import com.project.authservice.util.SecretsUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenServiceMapper {
  private final JwtKeyManager keyManager;

  public String mapAccessTokenForUser(User user, Instant now, Instant expiresAt, String issuer) {
    List<String> roles = user.getRoles().stream().map(Role::name).sorted().toList();
    JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
        .subject(String.valueOf(user.getId()))
        .claim(JwtClaims.TOKEN_USE, JwtClaims.TOKEN_USE_USER)
        .claim(JwtClaims.EMAIL, user.getEmail())
        .claim(JwtClaims.ROLES, roles);

    return sign(claims, now, expiresAt, issuer);
  }

  public String mapAccessTokenForClient(OAuthClient client, String scope, Instant now, Instant expiresAt, String issuer) {
    JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
        .subject(client.getClientId())
        .claim(JwtClaims.TOKEN_USE, JwtClaims.TOKEN_USE_CLIENT)
        .claim(JwtClaims.CLIENT_ID, client.getClientId())
        .claim(JwtClaims.SCOPE, scope);

    return sign(claims, now, expiresAt, issuer);
  }

  public RefreshToken mapRefreshToken(User user, String rawRefreshToken, Instant now, Duration refreshTokenTtl) {
    return RefreshToken.builder()
        .tokenHash(SecretsUtil.sha256Hex(rawRefreshToken))
        .user(user)
        .expiresAt(now.plus(refreshTokenTtl))
        .revoked(false)
        .build();
  }

  private String sign(JWTClaimsSet.Builder claims, Instant issuedAt, Instant expiresAt, String issuer) {
    JWTClaimsSet claimsSet = claims
        .issuer(issuer)
        .issueTime(Date.from(issuedAt))
        .expirationTime(Date.from(expiresAt))
        .jwtID(UUID.randomUUID().toString())
        .build();
    SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
        .keyID(keyManager.keyId())
        .type(JOSEObjectType.JWT)
        .build(), claimsSet);
    try {
      jwt.sign(keyManager.signer());
    } catch (JOSEException e) {
      throw new IllegalStateException("Cannot sign the access token", e);
    }
    return jwt.serialize();
  }
}
