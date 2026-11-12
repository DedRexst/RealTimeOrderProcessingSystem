package com.project.authservice.service.client.impl;

import com.project.authservice.dto.RegisterClientDto;
import com.project.authservice.entity.OAuthClient;
import com.project.authservice.repository.OAuthClientRepository;
import com.project.authservice.util.SecretsUtil;
import com.project.authservice.service.client.ClientService;
import com.project.authservice.service.client.mapper.ClientServiceMapper;
import com.project.authservice.service.token.TokenService;
import com.project.authservice.util.exception.OAuth2Exception;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

  private static final int CLIENT_ID_SUFFIX_BYTES = 4;
  private static final int MAX_CLIENT_ID_ATTEMPTS = 5;

  private final OAuthClientRepository clientRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  private final ClientServiceMapper clientServiceMapper;

  @Override
  @Transactional
  public OAuthClient register(RegisterClientDto req, String secret) {

    String clientId = generateClientId(req.name());
    Set<String> normalizeScopes = normalizeScopes(req.scopes());

    OAuthClient client = clientServiceMapper.mapClient(req, secret, normalizeScopes, clientId, passwordEncoder);

    client = clientRepository.save(client);
    log.info("Registered third-party client {} with scopes {}", client.getClientId(),
        client.getScopes());
    return client;
  }

  @Override
  @Transactional(readOnly = true)
  public String resolveEffectiveScope(Set<String> requestedScopes, Set<String> grantedScope) {
    Set<String> effective = requestedScopes.isEmpty() ? grantedScope : normalizeScopes(requestedScopes);
    if (!grantedScope.containsAll(effective)) {
      Set<String> refused = new TreeSet<>(effective);
      refused.removeAll(grantedScope);
      throw OAuth2Exception.invalidScope("Not granted to this client: " + String.join(" ", refused));
    }
    return String.join(" ", new TreeSet<>(effective));
  }

  @Override
  @Transactional(readOnly = true)
  public OAuthClient checkClientSecret(String clientId, String clientSecret) {
    OAuthClient client = clientRepository.findByClientId(clientId)
        .orElseThrow(() -> OAuth2Exception.invalidClient("Unknown client or bad secret"));
    if (!passwordEncoder.matches(clientSecret, client.getSecretHash())) {
      log.warn("Failed client_credentials attempt for {}", clientId);
      throw OAuth2Exception.invalidClient("Unknown client or bad secret");
    }
    if (!client.isEnabled()) {
      throw OAuth2Exception.invalidClient("Client is disabled");
    }
    return client;
  }

  @Override
  @Transactional
  public OAuthClient rotateSecret(String clientId, String secret) {
    OAuthClient client = getById(clientId);
    client.setSecretHash(passwordEncoder.encode(secret));
    client.setSecretRotatedAt(Instant.now());
    log.info("Rotated the secret of client {}", clientId);
    return client;
  }

  @Override
  @Transactional
  public OAuthClient setEnabled(String clientId, boolean enabled) {
    OAuthClient client = getById(clientId);
    client.setEnabled(enabled);
    log.info("Client {} is now {}", clientId, enabled ? "enabled" : "disabled");
    return client;
  }

  @Override
  @Transactional(readOnly = true)
  public OAuthClient getById(String clientId) {
    return clientRepository.findByClientId(clientId)
        .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<OAuthClient> findAll() {
    return clientRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OAuthClient> findEnabled(String clientId) {
    return Optional.ofNullable(clientId)
        .flatMap(clientRepository::findByClientId)
        .filter(OAuthClient::isEnabled);
  }

  /**
   * Readable in a log line, and still unguessable: the name makes the id recognisable while the
   * random suffix keeps two integrations of the same name apart.
   */
  private String generateClientId(String name) {
    String slug = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
    if (slug.length() > 32) {
      slug = slug.substring(0, 32);
    }
    if (slug.isEmpty()) {
      slug = "client";
    }
    for (int attempt = 0; attempt < MAX_CLIENT_ID_ATTEMPTS; attempt++) {
      String candidate = slug + "-" + SecretsUtil.randomToken(CLIENT_ID_SUFFIX_BYTES)
          .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
      if (!clientRepository.existsByClientId(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not allocate a unique client id for " + name);
  }

  private Set<String> normalizeScopes(Set<String> scopes) {
    Set<String> normalized = new HashSet<>();
    for (String scope : scopes) {
      if (scope != null && !scope.isBlank()) {
        normalized.add(scope.trim().toLowerCase(Locale.ROOT));
      }
    }
    return normalized;
  }
}
