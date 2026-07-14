package com.example.ch6project.domain.order.dto;

import com.example.ch6project.domain.order.entity.Order;
import com.example.ch6project.domain.payment.entity.Payment;
import com.example.ch6project.domain.point.entity.Point;

public record OrderResponse(
        Long id,
        Long userId,
        Long menuId,
        Long orderPrice,
        Long paymentAmount,
        Long balance,
        String orderStatus,
        String paymentStatus
) {
    public static OrderResponse from(Order order, Payment payment, Point  point) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getMenu().getId(),
                order.getOrderPrice(),
                payment.getAmount(),
                point.getBalance(),
                order.getStatus().name(),
                payment.getStatus().name()
        );
    }
}
