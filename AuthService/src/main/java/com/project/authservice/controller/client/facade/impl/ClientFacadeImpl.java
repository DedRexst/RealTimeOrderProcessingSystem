package com.project.authservice.controller.client.facade.impl;

import com.project.authservice.controller.client.facade.ClientFacade;
import com.project.authservice.controller.client.facade.mapper.ClientMapper;
import com.project.authservice.dto.ClientCredentialsDto;
import com.project.authservice.dto.ClientDto;
import com.project.authservice.dto.RegisterClientDto;
import com.project.authservice.entity.OAuthClient;
import com.project.authservice.service.client.ClientService;
import com.project.authservice.util.SecretsUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientFacadeImpl implements ClientFacade {

  private static final int SECRET_BYTES = 32;

  private final ClientService clientService;
  private final ClientMapper clientMapper;

  @Override
  public ClientCredentialsDto register(RegisterClientDto req) {
    String secret = SecretsUtil.randomToken(SECRET_BYTES);
    OAuthClient client = clientService.register(req, secret);

    ClientDto clientDto = clientMapper.toClientDto(client);
    return clientMapper.toClientCredentialsDto(clientDto, secret);
  }

  @Override
  public List<ClientDto> getAll() {
    return clientService.findAll().stream().map(clientMapper::toClientDto).toList();
  }

  @Override
  public ClientDto getById(String clientId) {
    OAuthClient client = clientService.getById(clientId);
    return clientMapper.toClientDto(client);
  }

  @Override
  public ClientCredentialsDto rotateSecret(String clientId) {
    String secret = SecretsUtil.randomToken(SECRET_BYTES);

    OAuthClient client = clientService.rotateSecret(clientId, secret);

    ClientDto clientDto = clientMapper.toClientDto(client);
    return clientMapper.toClientCredentialsDto(clientDto, secret);
  }

  @Override
  public ClientDto setEnabled(String clientId, boolean enebled) {
    OAuthClient client = clientService.setEnabled(clientId, enebled);
    return clientMapper.toClientDto(client);
  }
}
