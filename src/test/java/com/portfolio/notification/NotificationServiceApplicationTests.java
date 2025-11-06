package com.portfolio.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.rabbitmq.listener.simple.autoStartup=false",
    "spring.rabbitmq.listener.direct.autoStartup=false"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
