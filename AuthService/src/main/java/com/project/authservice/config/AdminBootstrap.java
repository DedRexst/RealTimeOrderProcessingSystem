package com.project.authservice.config;

import com.project.authservice.entity.Role;
import com.project.authservice.entity.User;
import com.project.authservice.repository.UserRepository;
import com.project.authservice.security.BootstrapProperties;
import java.util.EnumSet;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solves the chicken-and-egg of an empty database: client registration needs ROLE_ADMIN, and
 * nothing hands out that role yet. Idempotent — it only acts when the address is not taken.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auth.bootstrap", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class AdminBootstrap implements ApplicationRunner {

  private final BootstrapProperties properties;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    String email = properties.getAdminEmail().trim().toLowerCase(Locale.ROOT);
    if (userRepository.existsByEmailIgnoreCase(email)) {
      return;
    }
    userRepository.save(User.builder()
        .firstName("System")
        .lastName("Administrator")
        .email(email)
        .phone("+000000000")
        .password(passwordEncoder.encode(properties.getAdminPassword()))
        .state(User.UserState.ACTIVE)
        .deleted(false)
        .roles(EnumSet.of(Role.USER, Role.ADMIN))
        .build());
    log.warn("Created the bootstrap admin {}. Change its password and set "
        + "auth.bootstrap.enabled=false before this reaches anything but a laptop.", email);
  }
}
