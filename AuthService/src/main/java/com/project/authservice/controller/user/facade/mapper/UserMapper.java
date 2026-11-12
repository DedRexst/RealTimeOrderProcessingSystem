package com.project.authservice.controller.user.facade.mapper;

import com.project.authservice.dto.UserDto;
import com.project.authservice.entity.Role;
import com.project.authservice.entity.User;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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
}
