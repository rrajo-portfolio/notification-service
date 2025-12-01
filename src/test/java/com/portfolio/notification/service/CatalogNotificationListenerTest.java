package com.portfolio.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.notification.feed.NotificationEvent;
import com.portfolio.notification.feed.NotificationRegistry;
import com.portfolio.notification.feed.NotificationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CatalogNotificationListenerTest {

    private NotificationRegistry registry;
    private CatalogNotificationListener listener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        registry = Mockito.mock(NotificationRegistry.class);
        listener = new CatalogNotificationListener(objectMapper, registry);
    }

    @Test
    void emitsInfoNotificationForUpdates() throws Exception {
        UUID productId = UUID.randomUUID();
        String payload = """
            {
              "id": "%s",
              "type": "update",
              "payload": {
                "name": "API Toolkit",
                "sku": "SKU-1",
                "price": 19.99,
                "currency": "USD"
              }
            }
            """.formatted(productId);

        listener.onCatalogEvent(payload);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(registry).register(captor.capture());
        NotificationEvent event = captor.getValue();
        assertThat(event.title()).contains(productId.toString());
        assertThat(event.severity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(event.metadata()).containsEntry("name", "API Toolkit");
    }

    @Test
    void emitsWarningForDeleteEventsWithoutPayload() throws Exception {
        UUID productId = UUID.randomUUID();
        String payload = """
            {
              "id": "%s",
              "type": "delete"
            }
            """.formatted(productId);

        listener.onCatalogEvent(payload);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(registry).register(captor.capture());
        NotificationEvent event = captor.getValue();
        assertThat(event.severity()).isEqualTo(NotificationSeverity.WARNING);
        assertThat(event.metadata()).isEqualTo(Map.of());
    }

    @Test
    void ignoresInvalidPayloadsGracefully() {
        listener.onCatalogEvent("not-json");

        verify(registry, never()).register(any());
    }
}
