package com.project.authservice.controller.oauth2.facade;


import com.project.authservice.dto.TokenResponseDto;

public interface OAuth2TokenFacade {

  TokenResponseDto issueToken(String grantType, String clientId, String clientSecret, String scope, String authorization);
}
