package com.example.ch6project.domain.order.service;

import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.common.exception.ErrorCode;
import com.example.ch6project.domain.menu.entity.Menu;
import com.example.ch6project.domain.menu.repository.MenuRepository;
import com.example.ch6project.domain.order.dto.OrderRequest;
import com.example.ch6project.domain.order.dto.OrderResponse;
import com.example.ch6project.domain.order.entity.Order;
import com.example.ch6project.domain.order.repository.OrderRepository;
import com.example.ch6project.domain.payment.entity.Payment;
import com.example.ch6project.domain.payment.event.PaymentCompletedEvent;
import com.example.ch6project.domain.payment.event.PaymentCompletedProducer;
import com.example.ch6project.domain.payment.repository.PaymentRepository;
import com.example.ch6project.domain.point.entity.Point;
import com.example.ch6project.domain.point.repository.PointRepository;
import com.example.ch6project.domain.pointHistory.entity.PointHistory;
import com.example.ch6project.domain.pointHistory.repository.PointHistoryRepository;
import com.example.ch6project.domain.user.entity.User;
import com.example.ch6project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCompletedProducer paymentCompletedProducer;

    @Transactional
    public OrderResponse order(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        if (!menu.isActive()) {
            throw new CustomException(ErrorCode.MENU_INACTIVE);
        }

        Point point = pointRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_NOT_FOUND));

        if (point.getBalance() < menu.getPrice()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINT); // 수정된 예외
        }

        point.use(menu.getPrice());

        pointHistoryRepository.save(
                PointHistory.use(user, menu.getPrice(), point.getBalance())
        );

        Order order = orderRepository.save(Order.create(user, menu, menu.getPrice()));

        Payment payment = paymentRepository.save(Payment.create(order, user, menu.getPrice()));

        paymentCompletedProducer.send(
                PaymentCompletedEvent.builder()
                        .paymentId(payment.getId())
                        .orderId(order.getId())
                        .userId(user.getId())
                        .menuId(menu.getId())
                        .paymentAmount(payment.getAmount())
                        .paidAt(LocalDateTime.now().format(
                                DateTimeFormatter.ISO_DATE_TIME
                        ))
                        .build()
        );

        return OrderResponse.from(order, payment, point);
    }
}