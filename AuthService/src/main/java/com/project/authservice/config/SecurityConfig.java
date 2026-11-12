package com.project.authservice.config;

import com.project.authservice.security.BootstrapProperties;
import com.project.authservice.security.JwtAuthConverter;
import com.project.authservice.security.JwtKeyManager;
import com.project.authservice.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class, BootstrapProperties.class})
public class SecurityConfig {

  public static final String INTROSPECT_SCOPE = "tokens:introspect";

  private final JwtKeyManager keyManager;
  private final JwtProperties properties;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keyManager.publicKey())
        .signatureAlgorithm(SignatureAlgorithm.RS256)
        .build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
    return decoder;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) {
    return http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login",
                "/api/v1/auth/refresh", "/api/v1/auth/logout", "/oauth2/token").permitAll()
            .requestMatchers("/.well-known/**").permitAll()
            .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
            .permitAll()
            .requestMatchers("/api/v1/auth/introspect").hasAuthority("SCOPE_" + INTROSPECT_SCOPE)
            .requestMatchers("/api/v1/clients/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
            .decoder(jwtDecoder)
            .jwtAuthenticationConverter(new JwtAuthConverter())))
        .build();
  }
}
