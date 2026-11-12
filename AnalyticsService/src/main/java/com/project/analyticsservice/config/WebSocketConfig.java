package com.project.analyticsservice.config;

import com.project.analyticsservice.websocket.StatsWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

  private static final String STATS_ENDPOINT = "/ws/stats";

  private final StatsWebSocketHandler statsWebSocketHandler;

  @Value("${analytics.websocket.allowed-origins}")
  private String[] allowedOrigins;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(statsWebSocketHandler, STATS_ENDPOINT)
        .setAllowedOrigins(allowedOrigins);
  }
}
