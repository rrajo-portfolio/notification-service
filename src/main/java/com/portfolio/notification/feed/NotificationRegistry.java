package com.portfolio.notification.feed;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class NotificationRegistry {

    private static final Logger log = LoggerFactory.getLogger(NotificationRegistry.class);

    private final Deque<NotificationEvent> recentEvents = new ConcurrentLinkedDeque<>();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();
    private final int maxItems;

    public NotificationRegistry(@Value("${notification.feed.max-items:50}") int maxItems) {
        this.maxItems = Math.max(1, maxItems);
    }

    public void register(NotificationEvent event) {
        NotificationEvent enriched = event.toBuilder()
            .id(sequence.incrementAndGet())
            .createdAt(event.createdAt() != null ? event.createdAt() : Instant.now())
            .metadata(event.metadata() == null ? Map.of() : event.metadata())
            .build();
        recentEvents.addFirst(enriched);
        while (recentEvents.size() > maxItems) {
            recentEvents.removeLast();
        }
        emitters.forEach(emitter -> send(emitter, enriched));
    }

    public List<NotificationEvent> latest(int limit, Long sinceId) {
        List<NotificationEvent> snapshot = new ArrayList<>();
        int collected = 0;
        for (NotificationEvent event : recentEvents) {
            if (sinceId != null && event.id() <= sinceId) {
                continue;
            }
            snapshot.add(event);
            collected++;
            if (collected >= limit) {
                break;
            }
        }
        return snapshot;
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> {
            emitters.remove(emitter);
            log.debug("Removed SSE emitter due to error: {}", ex.getMessage());
        });
        emitters.add(emitter);
        return emitter;
    }

    private void send(SseEmitter emitter, NotificationEvent event) {
        try {
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(event, MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            emitters.remove(emitter);
            log.debug("Removed SSE emitter due to IO error: {}", ex.getMessage());
        }
    }

    public void registerSystemEvent(String title, String message, NotificationSeverity severity, Map<String, Object> metadata) {
        register(NotificationEvent.builder()
            .category("SYSTEM")
            .title(title)
            .message(message)
            .severity(severity)
            .metadata(metadata)
            .build());
    }
}
