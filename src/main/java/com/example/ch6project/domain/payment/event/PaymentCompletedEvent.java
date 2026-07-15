package com.example.ch6project.domain.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long menuId;
    private Long paymentAmount;
    private String paidAt;
}
