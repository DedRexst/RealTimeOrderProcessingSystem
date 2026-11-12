package com.project.authservice.controller.client;

import com.project.authservice.controller.client.facade.ClientFacade;
import com.project.authservice.dto.ClientCredentialsDto;
import com.project.authservice.dto.ClientDto;
import com.project.authservice.dto.RegisterClientDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administration of third-party systems. Every route here is behind {@code ROLE_ADMIN}, enforced
 * in {@link com.project.authservice.config.SecurityConfig}: registering a client is granting
 * standing access to the platform.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {

  private final ClientFacade clientFacade;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ClientCredentialsDto register(@Valid @RequestBody RegisterClientDto req) {
    return clientFacade.register(req);
  }

  @GetMapping
  public List<ClientDto> getAll() {
    return clientFacade.getAll();
  }

  @GetMapping("/{clientId}")
  public ClientDto getById(@PathVariable String clientId) {
    return clientFacade.getById(clientId);
  }

  /** Issues a new secret and invalidates the old one immediately — there is no overlap window. */
  @PostMapping("/{clientId}/secret")
  public ClientCredentialsDto rotateSecret(@PathVariable String clientId) {
    return clientFacade.rotateSecret(clientId);
  }

  @PostMapping("/{clientId}/enable")
  public ClientDto enable(@PathVariable String clientId) {
    return clientFacade.setEnabled(clientId, true);
  }

  /**
   * Cuts an integration off: no new tokens, and any token it already holds fails introspection.
   * A service that only checks signatures keeps honouring that token until it expires, which is
   * why the client token TTL is minutes rather than hours.
   */
  @PostMapping("/{clientId}/disable")
  public ClientDto disable(@PathVariable String clientId) {
    return clientFacade.setEnabled(clientId, false);
  }
}
