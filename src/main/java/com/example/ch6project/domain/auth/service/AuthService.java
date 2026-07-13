package com.example.ch6project.domain.auth.service;

import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.common.exception.ErrorCode;
import com.example.ch6project.common.security.JwtUtil;
import com.example.ch6project.domain.auth.dto.SignupRequest;
import com.example.ch6project.domain.auth.dto.SignupResponse;
import com.example.ch6project.domain.user.entity.User;
import com.example.ch6project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.LOGIN_ID_DUPLICATE);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.loginId(),
                encodedPassword,
                request.nickname()
        );

        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }
}
