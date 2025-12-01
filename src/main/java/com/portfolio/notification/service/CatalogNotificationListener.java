package com.portfolio.notification.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.notification.feed.NotificationEvent;
import com.portfolio.notification.feed.NotificationRegistry;
import com.portfolio.notification.feed.NotificationSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogNotificationListener {

    private final ObjectMapper objectMapper;
    private final NotificationRegistry notificationRegistry;

    @KafkaListener(
        topics = "${notification.kafka.product-topic:catalog-product-events}",
        groupId = "${notification.kafka.group-id:notification-service}"
    )
    public void onCatalogEvent(String payload) {
        try {
            ProductEventMessage event = objectMapper.readValue(payload, ProductEventMessage.class);
            String type = event.type() != null ? event.type().toUpperCase() : "UNKNOWN";
            String title = "Producto " + event.id();
            String message;
            NotificationSeverity severity = NotificationSeverity.INFO;
            if ("DELETE".equals(type)) {
                message = "Producto eliminado del catálogo";
                severity = NotificationSeverity.WARNING;
            } else {
                message = "Producto actualizado: " + event.payload().name();
                severity = NotificationSeverity.INFO;
            }
            Map<String, Object> metadata = event.payload() != null ? event.payload().toMetadata() : Map.of();
            notificationRegistry.register(NotificationEvent.builder()
                .category("CATALOG")
                .title(title)
                .message(message)
                .severity(severity)
                .createdAt(Instant.now())
                .metadata(metadata)
                .build());
        } catch (Exception ex) {
            log.warn("Failed to process catalog event payload {}", payload, ex);
        }
    }

    record ProductEventMessage(UUID id, String type, CatalogProductPayload payload) { }

    record CatalogProductPayload(String name, String sku, BigDecimal price, String currency) {
        Map<String, Object> toMetadata() {
            return Map.of(
                "name", name,
                "sku", sku,
                "price", price,
                "currency", currency
            );
        }
    }
}
