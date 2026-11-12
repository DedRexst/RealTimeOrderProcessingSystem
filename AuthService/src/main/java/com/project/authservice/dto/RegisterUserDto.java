package com.project.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(
    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @Email
    @NotBlank
    @Size(max = 255)
    String email,

    @NotBlank
    @Pattern(regexp = "\\+?[0-9 ()-]{7,20}", message = "must be a phone number")
    String phone,

    @NotBlank
    @Size(min = 10, max = 72, message = "must be between 10 and 72 characters")
    String password
) {}
