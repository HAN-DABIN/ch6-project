package com.example.ch6project.domain.order.entity;

import com.example.ch6project.common.entity.BaseTimeEntity;
import com.example.ch6project.domain.menu.entity.Menu;
import com.example.ch6project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    public Order(User user, Menu menu, Long orderPrice, OrderStatus status) {
        this.user = user;
        this.menu = menu;
        this.orderPrice = orderPrice;
        this.status = status;
    }

    public static Order create(User user, Menu menu, Long orderPrice) {
        return new Order(user, menu, orderPrice, OrderStatus.COMPLETED);
    }
}
