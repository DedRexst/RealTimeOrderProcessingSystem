package com.project.authservice.controller.auth.facade;

import com.project.authservice.dto.IntrospectionResponseDto;
import com.project.authservice.dto.LoginDto;
import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import org.springframework.transaction.annotation.Transactional;

public interface AuthFacade {
  @Transactional
  UserDto register(RegisterUserDto req);

  @Transactional
  TokenResponseDto login(LoginDto req);

  TokenResponseDto refresh(String rawRefreshToken);

  void revokeRefreshToken(String rawRefreshToken);

  IntrospectionResponseDto introspect(@NotBlank String token);
}
