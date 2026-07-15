package com.example.ch6project.domain.menu.ranking;

import com.example.ch6project.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MenuRankingConsumer {

    private final MenuRankingService menuRankingService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "menu-ranking-group",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void consume(PaymentCompletedEvent event) {
        LocalDate paidDate = LocalDateTime.parse(event.getPaidAt()).toLocalDate();
        menuRankingService.increaseMenuRanking(event.getMenuId(), paidDate);
    }
}
