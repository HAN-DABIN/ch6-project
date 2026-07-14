package com.example.ch6project.domain.point.entity;

import com.example.ch6project.common.entity.BaseTimeEntity;
import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.common.exception.ErrorCode;
import com.example.ch6project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "points")
public class Point extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", unique = true)
    private User user;

    @Column(nullable = false)
    private Long balance;

    private Point(User user, Long balance) {
        this.user = user;
        this.balance = balance;
    }

    public static Point create(User user) {
        return new Point(user, 0L);
    }

    public void charge(Long amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
        this.balance += amount;
    }

    public void use(Long amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_ORDER_AMOUNT);
        }

        if (this.balance < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINT);
        }

        this.balance -= amount;
    }
}
