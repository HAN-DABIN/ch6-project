package com.example.ch6project.domain.payment.entity;

import com.example.ch6project.common.entity.BaseTimeEntity;
import com.example.ch6project.domain.order.entity.Order;
import com.example.ch6project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    public Payment(Order order, User user, Long amount, PaymentStatus status) {
        this.order = order;
        this.user = user;
        this.amount = amount;
        this.status = status;
    }

    public static Payment create(Order order, User user, Long amount) {
        return new Payment(order, user, amount, PaymentStatus.COMPLETED);
    }
}
