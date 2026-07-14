package com.example.ch6project.domain.point.dto;

import com.example.ch6project.domain.point.entity.Point;

public record PointChargeResponse(
        Long id,
        Long chargeAmount,
        Long balance
) {
    public static PointChargeResponse from(Point point, Long  chargeAmount) {
        return new PointChargeResponse(
                point.getUser().getId(),
                chargeAmount,
                point.getBalance()
        );
    }
}
