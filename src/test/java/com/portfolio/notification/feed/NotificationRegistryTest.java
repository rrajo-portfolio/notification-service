package com.portfolio.notification.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationRegistryTest {

    private NotificationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NotificationRegistry(2);
    }

    @Test
    void registerEnrichesEventAndTrimsOldOnes() {
        registry.register(baseEvent("Product A"));
        List<NotificationEvent> firstSnapshot = registry.latest(10, null);
        assertThat(firstSnapshot).hasSize(1);
        NotificationEvent stored = firstSnapshot.get(0);
        assertThat(stored.id()).isPositive();
        assertThat(stored.createdAt()).isNotNull();
        assertThat(stored.metadata()).isEmpty();

        registry.register(baseEvent("Product B"));
        registry.register(baseEvent("Product C")); // pushes oldest out because max items = 2

        List<NotificationEvent> limited = registry.latest(10, null);
        assertThat(limited).hasSize(2);
        assertThat(limited.stream().map(NotificationEvent::title))
            .containsExactly("Product C", "Product B");
    }

    @Test
    void latestFiltersEventsBySinceIdAndLimit() {
        registry.register(baseEvent("First"));
        long firstId = registry.latest(5, null).get(0).id();
        registry.register(baseEvent("Second"));
        registry.register(baseEvent("Third"));

        List<NotificationEvent> filtered = registry.latest(1, firstId);
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).title()).isEqualTo("Third");
    }

    @Test
    void createEmitterRegistersAndRemovesEmitter() {
        SseEmitter emitter = registry.createEmitter();
        assertThat(emitter).isNotNull();
        emitter.complete();
    }

    @Test
    void registerSystemEventCreatesSystemNotification() {
        registry.registerSystemEvent("System", "Completed", NotificationSeverity.INFO, Map.of("source", "test"));
        List<NotificationEvent> events = registry.latest(5, null);
        assertThat(events).isNotEmpty();
        NotificationEvent systemEvent = events.get(0);
        assertThat(systemEvent.category()).isEqualTo("SYSTEM");
        assertThat(systemEvent.metadata()).containsEntry("source", "test");
    }

    private NotificationEvent baseEvent(String title) {
        return NotificationEvent.builder()
            .category("CATALOG")
            .title(title)
            .message("Message for " + title)
            .severity(NotificationSeverity.INFO)
            .build();
    }
}
