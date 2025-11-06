package com.portfolio.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfig {

    @Bean
    public Queue orderNotificationQueue(RabbitProperties properties) {
        return new Queue(properties.queueName(), true);
    }

    @Bean
    public TopicExchange orderExchange(RabbitProperties properties) {
        return new TopicExchange(properties.exchangeName(), true, false);
    }

    @Bean
    public Binding orderBinding(Queue orderNotificationQueue, TopicExchange orderExchange, RabbitProperties properties) {
        return BindingBuilder
            .bind(orderNotificationQueue)
            .to(orderExchange)
            .with(properties.routingKey());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, RabbitTemplateConfigurer configurer) {
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);
        return template;
    }
}

@ConfigurationProperties(prefix = "notification.rabbitmq")
record RabbitProperties(String queueName, String exchangeName, String routingKey) {
}
