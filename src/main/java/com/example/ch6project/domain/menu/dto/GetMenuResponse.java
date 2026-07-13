package com.example.ch6project.domain.menu.dto;

import com.example.ch6project.domain.menu.entity.Menu;

public record GetMenuResponse(
        Long id,
        String name,
        Long price
) {
    public static GetMenuResponse from(Menu menu) {
        return new GetMenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice());
    }
}
