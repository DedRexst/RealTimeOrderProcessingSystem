package com.project.authservice.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Turns the two authorization claims into Spring authorities: {@code roles} into {@code ROLE_*}
 * for people and {@code scope} into {@code SCOPE_*} for registered systems. Copy this class into
 * any service that consumes these tokens — it is the whole of the contract on the reading side.
 */
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    List<String> roles = jwt.getClaimAsStringList(JwtClaims.ROLES);
    if (roles != null) {
      roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    String scope = jwt.getClaimAsString(JwtClaims.SCOPE);
    if (scope != null && !scope.isBlank()) {
      for (String single : scope.split(" ")) {
        if (!single.isBlank()) {
          authorities.add(new SimpleGrantedAuthority("SCOPE_" + single));
        }
      }
    }
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }
}
