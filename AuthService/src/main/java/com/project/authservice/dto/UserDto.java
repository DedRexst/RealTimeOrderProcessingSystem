package com.project.authservice.dto;

import com.project.authservice.entity.User;
import java.time.Instant;
import java.util.Set;

public record UserDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    User.UserState state,
    Set<String> roles,
    Instant createdTime
) {
}
