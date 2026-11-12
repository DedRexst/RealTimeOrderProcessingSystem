package com.project.authservice.controller.oauth2.facade.model;

/**
 * A client's id and secret as presented on the token endpoint, whether they came from the HTTP
 * Basic header or from the form body.
 */
public record ClientCredentials(String id, String secret) {
}
