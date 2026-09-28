package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private final EmailService emailService;
    private final String orderCreatedTopic;

    public OrderCreatedConsumer(EmailService emailService, @Value("${notification.kafka.order-created-topic:order-created}") String orderCreatedTopic) {
        this.emailService = emailService;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    @KafkaListener(topics = "${notification.kafka.order-created-topic:order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String email) {
        if (email != null && !email.isEmpty()) {
            emailService.sendOrderCreatedEmail(email);
        }
    }
}

