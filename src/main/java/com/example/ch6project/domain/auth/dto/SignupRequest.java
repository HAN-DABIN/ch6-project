package com.example.ch6project.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(

        @NotBlank(message = "ID는 필수 입력입니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9]{4,20}$",
                message = "ID는 영문 대소문자와 숫자만 사용하여 4~20자로 입력해주세요.")
        String loginId,

        @NotBlank(message = "닉네임은 필수 입력입니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]{2,20}$",
                message = "닉네임은 한글, 영문, 숫자만 사용하여 2~20자로 입력해주세요."
        )
        String nickname,

        @NotBlank(message = "비밀번호는 필수 입력입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*]{8,20}$",
                message = "비밀번호는 영문과 숫자를 포함하여 8~20자로 입력해주세요."
        )
        String password
) {
}
