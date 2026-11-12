package com.project.authservice.controller.auth.facade.mapper;

import com.project.authservice.dto.IntrospectionResponseDto;
import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.OAuthClient;
import com.project.authservice.entity.Role;
import com.project.authservice.entity.User;
import com.project.authservice.security.JwtClaims;
import java.time.Instant;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  public UserDto toUserDto(User user) {
    return new UserDto(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPhone(),
        user.getState(),
        user.getRoles().stream().map(Role::name)
            .collect(Collectors.toCollection(TreeSet::new)),
        user.getCreatedTime());
  }

  public IntrospectionResponseDto toUserIntrospection(Jwt jwt, User user) {
    return new IntrospectionResponseDto(
        true,
        jwt.getSubject(),
        null,
        null,
        JwtClaims.TOKEN_USE_USER,
        user.getEmail(),
        user.getRoles().stream().map(Role::name).sorted().toList(),
        epochSecond(jwt.getExpiresAt()),
        epochSecond(jwt.getIssuedAt()),
        jwt.getIssuer().toString(),
        jwt.getId());
  }

  public IntrospectionResponseDto toClientIntrospection(Jwt jwt, OAuthClient client) {
    return new IntrospectionResponseDto(
        true,
        jwt.getSubject(),
        jwt.getClaimAsString(JwtClaims.SCOPE),
        client.getClientId(),
        JwtClaims.TOKEN_USE_CLIENT,
        null,
        null,
        epochSecond(jwt.getExpiresAt()),
        epochSecond(jwt.getIssuedAt()),
        jwt.getIssuer().toString(),
        jwt.getId());
  }

  private static Long epochSecond(Instant instant) {
    return instant == null ? null : instant.getEpochSecond();
  }
}
