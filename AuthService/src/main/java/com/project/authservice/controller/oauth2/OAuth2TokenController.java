package com.project.authservice.controller.oauth2;

import com.project.authservice.controller.oauth2.facade.OAuth2TokenFacade;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.service.client.ClientService;
import com.project.authservice.util.exception.OAuth2Exception;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The door for third-party systems: RFC 6749 §4.4 {@code client_credentials}, form-encoded, with
 * the client authenticating either by HTTP Basic or by body parameters. It is deliberately the
 * standard shape rather than something of our own, so an integrator can point an off-the-shelf
 * OAuth2 client at it and be done.
 */
@RestController
@RequiredArgsConstructor
public class OAuth2TokenController {

  private final OAuth2TokenFacade oAuth2TokenFacade;

  @PostMapping(path = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<TokenResponseDto> token(
      @RequestParam(name = "grant_type", required = false) String grantType,
      @RequestParam(name = "client_id", required = false) String clientId,
      @RequestParam(name = "client_secret", required = false) String clientSecret,
      @RequestParam(name = "scope", required = false) String scope,
      @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    TokenResponseDto token = oAuth2TokenFacade.issueToken(grantType, clientId, clientSecret, scope, authorization);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .header(HttpHeaders.PRAGMA, "no-cache")
        .body(token);
  }
}
