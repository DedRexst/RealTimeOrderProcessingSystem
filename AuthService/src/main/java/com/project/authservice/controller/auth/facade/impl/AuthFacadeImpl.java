package com.project.authservice.controller.auth.facade.impl;

import com.project.authservice.controller.auth.facade.AuthFacade;
import com.project.authservice.controller.auth.facade.mapper.AuthMapper;
import com.project.authservice.dto.IntrospectionResponseDto;
import com.project.authservice.dto.LoginDto;
import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.RefreshToken;
import com.project.authservice.entity.User;
import com.project.authservice.security.JwtClaims;
import com.project.authservice.service.client.ClientService;
import com.project.authservice.service.auth.AuthService;
import com.project.authservice.service.token.TokenService;
import com.project.authservice.service.user.UserService;
import com.project.authservice.util.UserCredentialsUtil;
import com.project.authservice.util.exception.BusinessException;
import com.project.authservice.util.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

  @Value("${security.jwt.access-token-ttl}")
  private Duration accessTokenTtl;

  private final AuthService authService;
  private final TokenService tokenService;
  private final UserService userService;
  private final ClientService clientService;

  private final AuthMapper authMapper;

  @Transactional
  @Override
  public UserDto register(RegisterUserDto req) {
    String email = UserCredentialsUtil.normalize(req.email());

    User user = authService.register(req, email);
    return authMapper.toUserDto(user);
  }

  @Transactional
  @Override
  public TokenResponseDto login(LoginDto req) {
    User user = authService.getUserIfLoginCredentialsAreValidAndUserActiveState(req);

    Instant now = Instant.now();

    String accessToken = tokenService.generateAccessToken(user, now);
    String rawRefreshToken = tokenService.generateRefreshToken(user, now);

    return TokenResponseDto.bearer(accessToken, accessTokenTtl.toSeconds(),
        rawRefreshToken, null);
  }

  @Transactional
  @Override
  public TokenResponseDto refresh(String rawRefreshToken) {
    Instant now = Instant.now();

    RefreshToken refreshToken = tokenService.getRefreshToken(rawRefreshToken);
    tokenService.isRefreshTokenValid(refreshToken, now);

    User user = refreshToken.getUser();
    if (!user.isActive()) {
      throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
    }

    refreshToken.setRevoked(true);
    refreshToken.setRevokedAt(now);

    String accessToken = tokenService.generateAccessToken(user, now);
    rawRefreshToken = tokenService.generateRefreshToken(user, now);


    return TokenResponseDto.bearer(accessToken, accessTokenTtl.toSeconds(),
        rawRefreshToken, null);
  }

  @Transactional
  @Override
  public void revokeRefreshToken(String rawRefreshToken) {
    tokenService.revokeRefreshToken(rawRefreshToken);
  }

  /**
   * Answers whether an access token would be honoured right now: a valid signature is not enough,
   * the account or client behind it must still be allowed to act — the part a service checking
   * the signature on its own cannot see.
   */
  @Transactional(readOnly = true)
  @Override
  public IntrospectionResponseDto introspect(String token) {
    return tokenService.decodeAccessToken(token)
        .flatMap(this::introspectDecoded)
        .orElse(IntrospectionResponseDto.INACTIVE);
  }

  private Optional<IntrospectionResponseDto> introspectDecoded(Jwt jwt) {
    String tokenUse = jwt.getClaimAsString(JwtClaims.TOKEN_USE);
    if (JwtClaims.TOKEN_USE_USER.equals(tokenUse)) {
      return parseUserId(jwt.getSubject())
          .flatMap(userService::findActiveById)
          .map(user -> authMapper.toUserIntrospection(jwt, user));
    }
    if (JwtClaims.TOKEN_USE_CLIENT.equals(tokenUse)) {
      return clientService.findEnabled(jwt.getClaimAsString(JwtClaims.CLIENT_ID))
          .map(client -> authMapper.toClientIntrospection(jwt, client));
    }
    return Optional.empty();
  }

  private static Optional<Long> parseUserId(String subject) {
    try {
      return Optional.of(Long.valueOf(subject));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
