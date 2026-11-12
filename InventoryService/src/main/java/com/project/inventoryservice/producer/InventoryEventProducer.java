package com.project.inventoryservice.producer;

import com.project.inventoryservice.event.InventoryReservedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    @Value("${kafka.topics.inventory-reserved}")
    private String inventoryReservedTopic;

    @Value("${kafka.topics.orders-dlq}")
    private String dlqTopic;

    private final KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;
    private final KafkaTemplate<String, Object> dlqKafkaTemplate;
    private final MeterRegistry meterRegistry;

    public void publishInventoryReserved(InventoryReservedEvent event) {
        kafkaTemplate.send(inventoryReservedTopic, event.getOrderId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish InventoryReservedEvent for orderId={}",
                        event.getOrderId(), ex);
                    meterRegistry.counter("kafka.producer.errors",
                        "topic", inventoryReservedTopic,
                        "service", "inventory-service").increment();
                } else {
                    log.info("Published InventoryReservedEvent: orderId={}, partition={}, offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                    meterRegistry.counter("kafka.producer.success",
                        "topic", inventoryReservedTopic).increment();
                }
            });
    }

    public void publishToDlq(InventoryReservedEvent originalEvent, String reason) {
        Headers headers = new RecordHeaders()
            .add("dlq-reason", reason.getBytes(StandardCharsets.UTF_8))
            .add("dlq-source-topic", "orders.created".getBytes(StandardCharsets.UTF_8))
            .add("dlq-timestamp",
                Instant.now().toString().getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> record = new ProducerRecord<>(
            dlqTopic,
            null,
            originalEvent.getOrderId().toString(),
            originalEvent,
            headers
        );

        dlqKafkaTemplate.send(record)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("CRITICAL: Failed to publish to DLQ for orderId={}",
                        originalEvent.getOrderId(), ex);
                } else {
                    log.warn("Event sent to DLQ: orderId={}, reason={}",
                        originalEvent.getOrderId(), reason);
                }
            });
    }
}
