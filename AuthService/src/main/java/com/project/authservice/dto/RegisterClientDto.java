package com.project.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RegisterClientDto(
    @NotBlank
    @Size(max = 120)
    String name,

    @Size(max = 500)
    String description,

    @Email
    String contactEmail,


    @NotEmpty
    @Size(max = 32)
    Set<@Pattern(regexp = "[a-z][a-z0-9_:.-]{1,63}",
        message = "must be lowercase, no spaces, e.g. orders:read") String> scopes
) {}
