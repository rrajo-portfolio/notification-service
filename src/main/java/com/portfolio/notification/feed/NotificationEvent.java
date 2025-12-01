package com.portfolio.notification.feed;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;

@Builder(toBuilder = true)
public record NotificationEvent(
    long id,
    String category,
    String title,
    String message,
    NotificationSeverity severity,
    Instant createdAt,
    Map<String, Object> metadata
) { }
