package com.project.authservice.service.client;

import com.project.authservice.dto.RegisterClientDto;
import com.project.authservice.entity.OAuthClient;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Registration and lifecycle of third-party systems, plus the token grant they use. */
public interface ClientService {

  /**
   * The only call that reveals a secret; afterwards only its hash exists.
   */
  OAuthClient register(RegisterClientDto req, String secret);

  /**
   * The space-separated scope the token will carry. {@code requestedScopes} may narrow what the
   * client was granted at registration, never widen it; empty means all of it.
   *
   * @throws com.project.authservice.util.exception.OAuth2Exception {@code invalid_scope} when a
   *     requested scope was never granted
   */
  String resolveEffectiveScope(Set<String> requestedScopes, Set<String> grantedScopes);

  OAuthClient checkClientSecret(String clientId, String clientSecret);

  OAuthClient rotateSecret(String clientId, String secret);

  OAuthClient setEnabled(String clientId, boolean enabled);

  OAuthClient getById(String clientId);

  List<OAuthClient> findAll();

  /** Empty for an unknown client and for one that has been disabled. */
  Optional<OAuthClient> findEnabled(String clientId);
}
