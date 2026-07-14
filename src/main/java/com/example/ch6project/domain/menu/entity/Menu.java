package com.example.ch6project.domain.menu.entity;

import com.example.ch6project.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status;

    public boolean isActive() {
        return this.status == MenuStatus.ACTIVE;
    }

    public Menu(String name, Long price, MenuStatus status) {
        this.name = name;
        this.price = price;
        this.status = status;
    }
}
