package com.project.authservice.controller.oauth2.facade.imple;

import com.project.authservice.controller.oauth2.facade.OAuth2TokenFacade;
import com.project.authservice.controller.oauth2.facade.model.ClientCredentials;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.entity.OAuthClient;
import com.project.authservice.service.client.ClientService;
import com.project.authservice.service.token.TokenService;
import com.project.authservice.util.ClientCredentialsUtil;
import com.project.authservice.util.exception.OAuth2Exception;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2TokenFacadeImpl implements OAuth2TokenFacade {

  @Value("${security.jwt.client-token-ttl}")
  private Duration clientTokenTtl;

  private static final String CLIENT_CREDENTIALS = "client_credentials";

  private final ClientService clientService;
  private final TokenService tokenService;

  @Override
  public TokenResponseDto issueToken(String grantType,
                                     String clientId,
                                     String clientSecret,
                                     String scope,
                                     String authorization) {
    requireClientCredentialsGrant(grantType);

    ClientCredentials credentials =
        ClientCredentialsUtil.resolve(authorization, clientId, clientSecret);
    OAuthClient client = clientService.checkClientSecret(credentials.id(), credentials.secret());
    String effectiveScopes =
        clientService.resolveEffectiveScope(parseScopes(scope), client.getScopes());
    String accessToken = tokenService.getAccessTokenForClient(client, effectiveScopes, clientTokenTtl);

    return TokenResponseDto.bearer(accessToken, clientTokenTtl.toSeconds(), null, effectiveScopes);
  }

  private void requireClientCredentialsGrant(String grantType) {
    if (isBlank(grantType)) {
      throw OAuth2Exception.invalidRequest("grant_type is required");
    }
    if (!CLIENT_CREDENTIALS.equals(grantType)) {
      throw OAuth2Exception.unsupportedGrantType(grantType);
    }
  }

  /**
   * Absent scope means "everything this client was granted"; see
   * ClientService#resolveEffectiveScope.
   */
  private Set<String> parseScopes(String scope) {
    if (isBlank(scope)) {
      return Set.of();
    }
    return Arrays.stream(scope.trim().split("\\s+"))
        .filter(single -> !single.isBlank())
        .collect(LinkedHashSet::new, Set::add, Set::addAll);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
