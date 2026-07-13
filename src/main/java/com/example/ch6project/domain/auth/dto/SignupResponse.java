package com.example.ch6project.domain.auth.dto;

import com.example.ch6project.domain.user.entity.User;

public record SignupResponse (
        Long id,
        String loginId,
        String nickname
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname()
        );
    }
}
