package com.project.authservice.controller.user;

import com.project.authservice.controller.user.facade.UserFacade;
import com.project.authservice.dto.UserDto;
import com.project.authservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserFacade userFacade;

  /**
   * The reference implementation of "validate this token for me": the caller sends the same
   * {@code Authorization: Bearer} header it would send to OrderService, and the subject claim is
   * resolved against the database rather than trusted from the token body.
   */
  @GetMapping("/me")
  public UserDto me(@AuthenticationPrincipal Jwt jwt) {
    return userFacade.getUserById(Long.valueOf(jwt.getSubject()));
  }
}
