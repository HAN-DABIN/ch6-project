package com.example.ch6project.domain.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull(message = "메뉴ID는 필수입니다.")
        Long menuId
) {
}
