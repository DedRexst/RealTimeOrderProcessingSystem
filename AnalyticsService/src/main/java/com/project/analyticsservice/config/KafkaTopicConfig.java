package com.project.analyticsservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Value("${kafka.topics.orders-stats}")
  private String ordersStatsTopic;

  @Value("${kafka.topics.orders-status-changed}")
  private String ordersStatusChangedTopic;

  @Value("${kafka.topics.orders-windowed-count}")
  private String ordersWindowedCountTopic;

  @Bean
  public NewTopic ordersStatsTopic() {
    return TopicBuilder.name(ordersStatsTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic ordersStatusChangedTopic() {
    return TopicBuilder.name(ordersStatusChangedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic ordersWindowedCountTopic() {
    return TopicBuilder.name(ordersWindowedCountTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }
}
