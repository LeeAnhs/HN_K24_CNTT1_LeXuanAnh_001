package org.example.orderservice.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${notification.kafka.order-created-topic:order-created}")
    private String orderCreatedTopic;

    public void sendOrderCreatedEvent(String customerEmail) {
        kafkaTemplate.send(orderCreatedTopic, customerEmail);
    }
}
