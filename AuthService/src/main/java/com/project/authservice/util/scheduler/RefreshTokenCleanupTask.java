package com.project.authservice.util.scheduler;

import com.project.authservice.repository.RefreshTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  @Scheduled(cron = "${auth.refresh-token-cleanup-cron:0 15 3 * * *}")
  public void purgeExpired() {
    int deleted = refreshTokenRepository.deleteExpiredBefore(Instant.now());
    if (deleted > 0) {
      log.info("Purged {} expired refresh token(s)", deleted);
    }
  }
}
