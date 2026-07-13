package com.example.ch6project.common.exception;

import com.example.ch6project.common.response.CommonApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 서비스 계층에서 발생한 비즈니스 예외를 처리
     * CustomException에 담긴 ErrorCode를 이용하여 공통 응답 형식으로 반환
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonApiResponse.error(errorCode));
    }

    /**
     * DTO 유효성 검증 실패 시 발생한 예외를 처리
     * 검증 실패 메시지 목록을 공통 응답 형식으로 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<List<String>>> handleValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonApiResponse.error(errorCode, errors));
    }

    /**
     * 처리되지 않은 모든 예외를 처리
     * 예상하지 못한 서버 오류 발생 시 INTERNAL_SERVER_ERROR 응답을 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonApiResponse.error(errorCode));
    }
}
