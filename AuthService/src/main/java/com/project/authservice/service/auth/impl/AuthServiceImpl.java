package com.project.authservice.service.auth.impl;

import com.project.authservice.dto.LoginDto;
import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.dto.TokenResponseDto;
import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.User;
import com.project.authservice.service.token.TokenService;
import com.project.authservice.service.auth.AuthService;
import com.project.authservice.service.auth.mapper.AuthServiceMapper;
import com.project.authservice.service.user.UserService;
import com.project.authservice.util.UserCredentialsUtil;
import com.project.authservice.util.exception.BusinessException;
import com.project.authservice.util.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  private final AuthServiceMapper authServiceMapper;

  @Override
  @Transactional
  public User register(RegisterUserDto req, String email) {
    userService.emailExistCheck(email);

    String passwordEncoded = passwordEncoder.encode(req.password());
    User user = authServiceMapper.mapNewUser(req, email, passwordEncoded);

    user = userService.save(user);
    log.info("Registered user {}", user.getId());
    return user;
  }

  @Override
  @Transactional
  public User getUserIfLoginCredentialsAreValidAndUserActiveState(LoginDto req) {
    String email = UserCredentialsUtil.normalize(req.email());
    User user = userService.findByEmailIgnoreCase(email);

    if(!user.isActive()) {
      throw new BusinessException(ErrorCode.USER_DELETED);
    }
    boolean passwordMatches = passwordEncoder.matches(req.password(), user.getPassword());
    if (!passwordMatches) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD);
    }
    return user;
  }
}
