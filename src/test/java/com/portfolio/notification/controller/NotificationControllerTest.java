package com.portfolio.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.portfolio.notification.feed.NotificationEvent;
import com.portfolio.notification.feed.NotificationRegistry;
import com.portfolio.notification.feed.NotificationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationControllerTest {

    private NotificationRegistry registry;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        registry = mock();
        controller = new NotificationController(registry);
    }

    @Test
    void latestClampsLimitAndReturnsLastEventId() {
        NotificationEvent event = NotificationEvent.builder()
            .id(12L)
            .category("ORDER")
            .title("Pedido 12")
            .message("Creado")
            .severity(NotificationSeverity.SUCCESS)
            .createdAt(Instant.now())
            .metadata(Map.of())
            .build();
        when(registry.latest(anyInt(), eq(null))).thenReturn(List.of(event));

        NotificationController.NotificationFeedResponse response = controller.latest(500, null);

        verify(registry).latest(eq(100), eq(null));
        assertThat(response.count()).isEqualTo(1);
        assertThat(response.lastEventId()).isEqualTo(12L);
        assertThat(response.items()).containsExactly(event);
    }

    @Test
    void latestReturnsSinceIdWhenNoEvents() {
        when(registry.latest(anyInt(), eq(7L))).thenReturn(List.of());

        NotificationController.NotificationFeedResponse response = controller.latest(-5, 7L);

        verify(registry).latest(eq(1), eq(7L));
        assertThat(response.count()).isZero();
        assertThat(response.lastEventId()).isEqualTo(7L);
    }
}
