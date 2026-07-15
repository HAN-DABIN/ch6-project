package com.example.ch6project.domain.payment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCompletedProducer {

    private static final String TOPIC = "payment-completed";

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    public void send(PaymentCompletedEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getMenuId()), event);
    }
}
