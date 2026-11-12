package com.project.analyticsservice.client;

import com.project.analyticsservice.util.StatsMessageType;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebhookClient {

  private final RestClient restClient;

  public WebhookClient(@Value("${webhook.url:}") String webhookUrl) {
    if (webhookUrl == null || webhookUrl.isBlank()) {
      log.info("No webhook.url configured; analytics go to connected dashboards only");
      this.restClient = null;
    } else {
      this.restClient = RestClient.builder().baseUrl(webhookUrl).build();
    }
  }

 public void send(StatsMessageType type, Object data) {
    if (restClient == null) {
      return;
    }
    restClient.post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("type", type, "data", data))
        .retrieve()
        .toBodilessEntity();
    log.debug("Webhook sent: type={}", type);
  }
}
