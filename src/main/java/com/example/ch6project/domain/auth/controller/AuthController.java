package com.example.ch6project.domain.auth.controller;

import com.example.ch6project.common.response.CommonApiResponse;
import com.example.ch6project.domain.auth.dto.LoginRequest;
import com.example.ch6project.domain.auth.dto.LoginResponse;
import com.example.ch6project.domain.auth.dto.SignupRequest;
import com.example.ch6project.domain.auth.dto.SignupResponse;
import com.example.ch6project.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<CommonApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonApiResponse.success(HttpStatus.OK,"로그인을 성공했습니다.", response));
    }
}
