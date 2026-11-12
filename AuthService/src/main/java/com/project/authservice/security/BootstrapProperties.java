package com.project.authservice.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.bootstrap")
public class BootstrapProperties {

  /** Turn this off once a real admin exists; it is a convenience for a fresh database. */
  private boolean enabled = true;

  private String adminEmail = "admin@example.com";

  private String adminPassword = "Admin123!";
}
