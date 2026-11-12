package com.project.authservice.repository;

import com.project.authservice.entity.OAuthClient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {

  Optional<OAuthClient> findByClientId(String clientId);

  boolean existsByClientId(String clientId);
}
