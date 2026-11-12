package com.project.authservice.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * What lets the browser frontend call this service at all: without it a cross-origin login is
 * blocked before the request is ever sent.
 *
 * <p>Credentials are deliberately not allowed. Every call here carries a bearer token in a header
 * that the frontend sets explicitly, so there is no cookie for a browser to attach on its own —
 * and not allowing them is what makes {@code Access-Control-Allow-Origin} safe to widen by
 * configuration without opening a CSRF path.
 */
@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${security.cors.allowed-origins}") List<String> allowedOrigins) {

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    // The frontend reads nothing but the body, so nothing needs exposing beyond the safelist.
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
