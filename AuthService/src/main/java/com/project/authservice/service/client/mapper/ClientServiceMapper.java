package com.project.authservice.service.client.mapper;

import com.project.authservice.dto.RegisterClientDto;
import com.project.authservice.entity.OAuthClient;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ClientServiceMapper {


  public OAuthClient mapClient(RegisterClientDto req, String secret, Set<String> normalizeScopes, String clientId, PasswordEncoder passwordEncoder) {
    return OAuthClient.builder()
        .clientId(clientId)
        .name(req.name().trim())
        .description(req.description())
        .contactEmail(req.contactEmail())
        .secretHash(passwordEncoder.encode(secret))
        .scopes(normalizeScopes)
        .enabled(true)
        .build();
  }
}
