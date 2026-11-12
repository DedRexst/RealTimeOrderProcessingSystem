package com.project.authservice.controller.user.facade.impl;

import com.project.authservice.controller.user.facade.UserFacade;
import com.project.authservice.controller.user.facade.mapper.UserMapper;
import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.User;
import com.project.authservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {
  private final UserService userService;
  private final UserMapper userMapper;

  @Override
  public UserDto getUserById(Long userId) {
    User user = userService.getUserById(userId);

    return userMapper.toUserDto(user);
  }
}
