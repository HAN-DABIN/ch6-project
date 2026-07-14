package com.example.ch6project.domain.order.controller;

import com.example.ch6project.common.response.CommonApiResponse;
import com.example.ch6project.domain.order.dto.OrderRequest;
import com.example.ch6project.domain.order.dto.OrderResponse;
import com.example.ch6project.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CommonApiResponse<OrderResponse>> order(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.order(userId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonApiResponse.success(HttpStatus.OK, "커피 주문 및 결제 성공", response));
    }
}
