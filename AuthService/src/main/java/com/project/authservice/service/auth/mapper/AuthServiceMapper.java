package com.project.authservice.service.auth.mapper;

import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.entity.Role;
import com.project.authservice.entity.User;
import java.util.EnumSet;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceMapper {

  public User mapNewUser(RegisterUserDto req, String email, String password) {
    return User.builder()
        .firstName(req.firstName().trim())
        .lastName(req.lastName().trim())
        .email(email)
        .phone(req.phone().trim())
        .password(password)
        .state(User.UserState.ACTIVE)
        .deleted(false)
        .roles(EnumSet.of(Role.USER))
        .build();
  }
}
