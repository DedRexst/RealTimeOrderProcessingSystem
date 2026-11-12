package com.project.orderservice.configuration;

import com.project.orderservice.security.JwtAuthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Every call into this service now carries a token AuthService signed. The key is fetched from its
 * JWK set once and cached, so verification is local: AuthService going down stops new logins, not
 * order traffic.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** What a third-party system needs to move an order that is not its own. */
  public static final String ORDERS_WRITE = "SCOPE_orders:write";

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
            .permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status")
            .hasAnyAuthority("ROLE_ADMIN", ORDERS_WRITE)
            .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/status-callbacks")
            .hasAuthority(ORDERS_WRITE)
            .requestMatchers(HttpMethod.POST, "/api/v1/catalog/products").hasAuthority("ROLE_ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
            jwt.jwtAuthenticationConverter(new JwtAuthConverter())))
        .build();
  }
}
