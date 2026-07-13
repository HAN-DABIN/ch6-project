package com.example.ch6project.common.response;

import com.example.ch6project.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonApiResponse<T> {

    private final int status;
    private final String code;
    private final String message;
    private final T data;

    private CommonApiResponse(int status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonApiResponse<T> success(HttpStatus status, String message, T data) {
        return new CommonApiResponse<>(status.value(), null, message, data);
    }

    public static CommonApiResponse<Void> error(ErrorCode errorCode) {
        return new CommonApiResponse<>(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public static <T> CommonApiResponse<T> error(ErrorCode errorCode, T data) {
        return new CommonApiResponse<>(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );
    }
}
