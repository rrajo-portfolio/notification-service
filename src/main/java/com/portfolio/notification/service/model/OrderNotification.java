package com.portfolio.notification.service.model;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderNotification(
    UUID orderId,
    String status,
    BigDecimal totalAmount,
    String recipientEmail
) {
}
