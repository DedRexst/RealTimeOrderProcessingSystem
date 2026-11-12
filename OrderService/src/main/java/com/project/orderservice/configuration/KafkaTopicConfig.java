package com.project.orderservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Value("${kafka.topics.orders-created}")
  private String ordersCreatedTopic;

  @Value("${kafka.topics.inventory-reserved}")
  private String inventoryReservedTopic;

  @Value("${kafka.topics.orders-notifications}")
  private String ordersNotificationsTopic;

  @Value("${kafka.topics.orders-status-changed}")
  private String ordersStatusChangedTopic;

  @Value("${kafka.topics.products-created}")
  private String productsCreatedTopic;

  @Bean
  public NewTopic ordersCreatedTopic() {
    return TopicBuilder.name(ordersCreatedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4")
        .build();
  }

  /**
   * Also declared by InventoryService. Topic creation is idempotent, and declaring the topic this
   * service consumes removes the dependency on which of the two starts first — auto-creation is
   * disabled on the broker.
   */
  @Bean
  public NewTopic inventoryReservedTopic() {
    return TopicBuilder.name(inventoryReservedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  /**
   * Consumed by NotificationService. Same partition count as orders.created so that keying by
   * orderId keeps a customer's updates in order, and a longer retention because an undelivered
   * email is worth replaying for longer than an analytics record.
   */
  @Bean
  public NewTopic ordersNotificationsTopic() {
    return TopicBuilder.name(ordersNotificationsTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "1209600000")
        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4")
        .build();
  }

  /**
   * Consumed by AnalyticsService, which relays each transition as a webhook. Same partition count
   * and key as the notification topic so a subscriber sees an order's transitions in the order they
   * happened, and a week of retention — long enough to replay a webhook endpoint's outage.
   */
  @Bean
  public NewTopic ordersStatusChangedTopic() {
    return TopicBuilder.name(ordersStatusChangedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4")
        .build();
  }

  /**
   * Consumed by InventoryService, which creates the product's stock row. Also declared there, so
   * neither service depends on the other starting first.
   */
  @Bean
  public NewTopic productsCreatedTopic() {
    return TopicBuilder.name(productsCreatedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }
}
