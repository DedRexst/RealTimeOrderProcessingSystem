package com.project.authservice.controller.client.facade.mapper;

import com.project.authservice.dto.ClientCredentialsDto;
import com.project.authservice.dto.ClientDto;
import com.project.authservice.entity.OAuthClient;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
  public ClientCredentialsDto toClientCredentialsDto(ClientDto client, String clientSecret) {
    return new ClientCredentialsDto(client,
        clientSecret,
        "Store this secret now: it is not recoverable and can only be replaced by rotating it.");
  }

  public ClientDto toClientDto(OAuthClient client) {
    return new ClientDto(
        client.getClientId(),
        client.getName(),
        client.getDescription(),
        client.getContactEmail(),
        new TreeSet<>(client.getScopes()),
        client.isEnabled(),
        client.getCreatedTime(),
        client.getSecretRotatedAt());
  }
}
