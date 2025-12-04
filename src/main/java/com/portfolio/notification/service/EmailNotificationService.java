package com.portfolio.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.notification.feed.NotificationEvent;
import com.portfolio.notification.feed.NotificationRegistry;
import com.portfolio.notification.feed.NotificationSeverity;
import com.portfolio.notification.service.model.OrderNotification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final NotificationRegistry notificationRegistry;

    @Value("${notification.mail.from:portfolio@notification.local}")
    private String fromAddress;

    @Value("${notification.mail.to:talent@portfolio.local}")
    private String defaultRecipient;

    @PostConstruct
    void logConfiguredRecipient() {
        log.info("Notification emails will be sent from '{}' to '{}'", fromAddress, defaultRecipient);
    }

    @RabbitListener(queues = "${notification.rabbitmq.queue-name}")
    public void onOrderMessage(String payload) {
        try {
            OrderNotification notification = objectMapper.readValue(payload, OrderNotification.class);
            sendEmail(notification);
              registerFeedEvent(notification);
            log.info("Processed notification for order {}", notification.orderId());
        } catch (Exception ex) {
            log.error("Failed to process order notification payload {}", payload, ex);
        }
    }

    private void sendEmail(OrderNotification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(notification.recipientEmail() != null ? notification.recipientEmail() : defaultRecipient);
        message.setSubject("Order " + notification.orderId() + " status: " + notification.status());
        message.setText("Hello,\n\nOrder " + notification.orderId() + " is now " + notification.status()
            + ".\nTotal amount: " + notification.totalAmount()
            + "\n\nThanks,\nPortfolio Notification Service");
        try {
            mailSender.send(message);
        } catch (MailException mailException) {
            log.warn("Failed to send email for order {}: {}", notification.orderId(), mailException.getMessage());
        }
    }

    private void registerFeedEvent(OrderNotification notification) {
        notificationRegistry.register(NotificationEvent.builder()
            .category("ORDER")
            .title("Pedido " + notification.orderId())
            .message("Nuevo estado: " + notification.status() + " · Total " + notification.totalAmount())
            .severity("CANCELLED".equalsIgnoreCase(notification.status()) ? NotificationSeverity.WARNING : NotificationSeverity.SUCCESS)
            .metadata(Map.of(
                "orderId", notification.orderId(),
                "status", notification.status(),
                "totalAmount", notification.totalAmount()
            ))
            .build());
    }
}
