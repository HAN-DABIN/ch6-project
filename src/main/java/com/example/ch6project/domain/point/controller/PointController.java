package com.example.ch6project.domain.point.controller;

import com.example.ch6project.common.response.CommonApiResponse;
import com.example.ch6project.domain.point.dto.PointChargeRequest;
import com.example.ch6project.domain.point.dto.PointChargeResponse;
import com.example.ch6project.domain.point.service.PointService;
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
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public ResponseEntity<CommonApiResponse<PointChargeResponse>> chargePoint(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PointChargeRequest request) {
        PointChargeResponse response = pointService.charge(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.success(HttpStatus.CREATED,"포인트가 충전되었습니다.", response));

    }
}
