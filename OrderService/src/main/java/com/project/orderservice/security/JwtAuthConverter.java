package com.project.orderservice.security;

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
 * Turns the two authorization claims AuthService writes into Spring authorities: {@code roles}
 * into {@code ROLE_*} for people and {@code scope} into {@code SCOPE_*} for registered systems.
 *
 * <p>A copy of the class of the same name in AuthService. It is duplicated rather than shared
 * because a shared module would couple this service's build to the auth service's release cycle
 * for thirty lines; the claim names it reads are the actual contract, and they are stated in
 * AuthService's README.
 */
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLES_CLAIM = "roles";
  private static final String SCOPE_CLAIM = "scope";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
    if (roles != null) {
      roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    String scope = jwt.getClaimAsString(SCOPE_CLAIM);
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
