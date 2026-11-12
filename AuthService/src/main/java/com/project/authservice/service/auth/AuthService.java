package com.project.authservice.service.auth;

import com.project.authservice.dto.LoginDto;
import com.project.authservice.dto.RegisterUserDto;
import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.User;

public interface AuthService {

  User register(RegisterUserDto req, String email);

  User getUserIfLoginCredentialsAreValidAndUserActiveState(LoginDto req);
}
