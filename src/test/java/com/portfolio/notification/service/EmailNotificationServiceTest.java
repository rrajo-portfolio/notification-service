package com.portfolio.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.notification.feed.NotificationRegistry;
import com.portfolio.notification.service.model.OrderNotification;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRegistry notificationRegistry;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailNotificationService = new EmailNotificationService(mailSender, new ObjectMapper(), notificationRegistry);
        ReflectionTestUtils.setField(emailNotificationService, "fromAddress", "from@test.local");
        ReflectionTestUtils.setField(emailNotificationService, "defaultRecipient", "default@test.local");
    }

    @Test
    @DisplayName("onOrderMessage should send email when payload is valid")
    void onOrderMessageSendsEmail() throws Exception {
        OrderNotification notification = new OrderNotification(UUID.randomUUID(), "CONFIRMED", BigDecimal.TEN, "user@test.local");
        String payload = new ObjectMapper().writeValueAsString(notification);

        emailNotificationService.onOrderMessage(payload);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
