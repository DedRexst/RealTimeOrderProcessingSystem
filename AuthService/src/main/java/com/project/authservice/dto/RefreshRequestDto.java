package com.project.authservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** Accepts both spellings so a caller written against the OAuth2 wire format is not surprised. */
public record RefreshRequestDto(
    @NotBlank
    @JsonAlias("refreshToken")
    @JsonProperty("refresh_token")
    String refreshToken
) {}
