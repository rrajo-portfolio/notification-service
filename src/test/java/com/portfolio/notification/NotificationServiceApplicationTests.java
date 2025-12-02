package com.portfolio.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.rabbitmq.listener.simple.autoStartup=false",
    "spring.rabbitmq.listener.direct.autoStartup=false"
})
class NotificationServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        assertThat(applicationContext).as("Notification context should load").isNotNull();
    }
}
