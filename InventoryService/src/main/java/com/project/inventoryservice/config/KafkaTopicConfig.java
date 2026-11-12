package com.project.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
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

    @Value("${kafka.topics.orders-dlq}")
    private String ordersDlqTopic;

    @Value("${kafka.topics.products-created}")
    private String productsCreatedTopic;

    @Bean
    public NewTopic ordersCreatedTopic() {
        return TopicBuilder.name(ordersCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(inventoryReservedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersDlqTopic() {
        return TopicBuilder.name(ordersDlqTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** Also declared by OrderService, which produces it; creation is idempotent. */
    @Bean
    public NewTopic productsCreatedTopic() {
        return TopicBuilder.name(productsCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
