package com.project.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
    @NotBlank
    String email,

    @NotBlank
    String password
) {}
