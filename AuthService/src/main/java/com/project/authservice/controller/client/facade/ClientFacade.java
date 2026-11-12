package com.project.authservice.controller.client.facade;

import com.project.authservice.dto.ClientCredentialsDto;
import com.project.authservice.dto.ClientDto;
import com.project.authservice.dto.RegisterClientDto;
import jakarta.validation.Valid;
import java.util.List;

public interface ClientFacade {
  ClientCredentialsDto register(@Valid RegisterClientDto req);

  List<ClientDto> getAll();

  ClientDto getById(String clientId);

  ClientCredentialsDto rotateSecret(String clientId);

  ClientDto setEnabled(String clientId, boolean b);
}
