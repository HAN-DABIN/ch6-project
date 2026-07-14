package com.example.ch6project.domain.pointHistory.entity;

import com.example.ch6project.common.entity.BaseTimeEntity;
import com.example.ch6project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_histories")
public class PointHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type;

    @Column(nullable = false)
    private Long balanceAfter;

    public PointHistory(User user, Long amount, PointType type, Long balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
    }

    public static PointHistory charge(User user, Long amount, Long balanceAfter) {
        return new PointHistory(user, amount, PointType.CHARGE, balanceAfter);
    }

    public static PointHistory use(User user, Long amount, Long balanceAfter) {
        return new PointHistory(user, amount, PointType.USE, balanceAfter);
    }
}
