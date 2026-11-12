package com.project.authservice.controller.user.facade;

import com.project.authservice.dto.UserDto;

public interface UserFacade {
  UserDto getUserById(Long aLong);
}
