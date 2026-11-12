package com.project.authservice.dto;

import com.project.authservice.entity.OAuthClient;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

/** A registered third-party system as an operator sees it — never including the secret. */
public record ClientDto(
    String clientId,
    String name,
    String description,
    String contactEmail,
    Set<String> scopes,
    boolean enabled,
    Instant createdTime,
    Instant secretRotatedAt
) {}
