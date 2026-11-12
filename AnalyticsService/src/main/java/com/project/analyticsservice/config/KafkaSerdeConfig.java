package com.project.analyticsservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.analyticsservice.avro.CustomerOrderStats;
import com.project.analyticsservice.event.OrderCreatedEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaSerdeConfig {

  @Value("${kafka.schema-registry-url}")
  private String schemaRegistryUrl;

  @Bean
  public Serde<OrderCreatedEvent> orderCreatedEventSerde() {
    ObjectMapper mapper = new ObjectMapper();
    return Serdes.serdeFrom(
        (topic, event) -> {
          try {
            return mapper.writeValueAsBytes(event);
          } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize OrderCreatedEvent", e);
          }
        },
        (topic, bytes) -> {
          if (bytes == null) {
            return null;
          }
          try {
            return mapper.readValue(bytes, OrderCreatedEvent.class);
          } catch (IOException e) {
            throw new SerializationException("Failed to deserialize OrderCreatedEvent", e);
          }
        });
  }

  @Bean
  public SpecificAvroSerde<CustomerOrderStats> customerOrderStatsSerde() {
    SpecificAvroSerde<CustomerOrderStats> serde = new SpecificAvroSerde<>();
    serde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl),
        false);
    return serde;
  }
}
