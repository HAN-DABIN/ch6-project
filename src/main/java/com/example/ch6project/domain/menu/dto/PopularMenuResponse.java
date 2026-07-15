package com.example.ch6project.domain.menu.dto;

import com.example.ch6project.domain.menu.entity.Menu;

public record PopularMenuResponse(
        Long menuId,
        String name,
        Long price,
        Long orderCount
) {
    public static PopularMenuResponse from(Menu menu, Long orderCount) {
        return new PopularMenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                orderCount
        );
    }
}