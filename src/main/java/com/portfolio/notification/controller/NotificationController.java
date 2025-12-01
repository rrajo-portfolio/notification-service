package com.portfolio.notification.controller;

import java.util.List;

import com.portfolio.notification.feed.NotificationEvent;
import com.portfolio.notification.feed.NotificationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRegistry registry;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationFeedResponse latest(
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) Long sinceId
    ) {
        List<NotificationEvent> items = registry.latest(Math.min(Math.max(limit, 1), 100), sinceId);
        Long lastId = items.isEmpty() ? sinceId : Long.valueOf(items.get(0).id());
        return new NotificationFeedResponse(items, lastId, items.size());
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream() {
        return registry.createEmitter();
    }

    public record NotificationFeedResponse(
        List<NotificationEvent> items,
        Long lastEventId,
        int count
    ) { }
}
