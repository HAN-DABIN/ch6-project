package com.example.ch6project.domain.point.service;

import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.common.exception.ErrorCode;
import com.example.ch6project.domain.point.dto.PointChargeRequest;
import com.example.ch6project.domain.point.dto.PointChargeResponse;
import com.example.ch6project.domain.point.entity.Point;
import com.example.ch6project.domain.point.repository.PointRepository;
import com.example.ch6project.domain.pointHistory.entity.PointHistory;
import com.example.ch6project.domain.pointHistory.repository.PointHistoryRepository;
import com.example.ch6project.domain.user.entity.User;
import com.example.ch6project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public PointChargeResponse charge(Long userId, PointChargeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> pointRepository.save(Point.create(user)));

        point.charge(request.amount());

        pointHistoryRepository.save(
                PointHistory.charge(user, request.amount(), point.getBalance())
        );

        return PointChargeResponse.from(point, request.amount());
    }
}
