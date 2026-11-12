package com.project.authservice.controller.auth;

import com.project.authservice.controller.auth.facade.AuthFacade;
import com.project.authservice.dto.IntrospectionResponseDto;
import com.project.authservice.dto.LoginDto;
import com.project.authservice.dto.RefreshRequestDto;
import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.dto.UserDto;
import com.project.authservice.service.token.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthFacade authFacade;
  private final TokenService tokenService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto register(@Valid @RequestBody RegisterUserDto req) {
    return authFacade.register(req);
  }

  @PostMapping("/login")
  public TokenResponseDto login(@Valid @RequestBody LoginDto req) {
    return authFacade.login(req);
  }

  @PostMapping("/refresh")
  public TokenResponseDto refresh(@Valid @RequestBody RefreshRequestDto req) {
    return authFacade.refresh(req.refreshToken());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody RefreshRequestDto req) {
    authFacade.revokeRefreshToken(req.refreshToken());
  }

  @PostMapping(path = "/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public IntrospectionResponseDto introspect(@RequestParam @NotBlank String token) {
    return authFacade.introspect(token);
  }
}
