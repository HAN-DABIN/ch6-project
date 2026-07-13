package com.example.ch6project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_CHARGE_AMOUNT", "충전 금액은 0보다 커야 합니다."),
    INVALID_MENU_PRICE(HttpStatus.BAD_REQUEST, "INVALID_MENU_PRICE", "메뉴 가격이 올바르지 않습니다."),
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_ORDER_AMOUNT", "주문 금액이 올바르지 않습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_NOT_FOUND", "포인트 정보를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),

    // 409 CONFLICT
    MENU_INACTIVE(HttpStatus.CONFLICT, "MENU_INACTIVE", "주문할 수 없는 메뉴입니다."),
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "INSUFFICIENT_POINT", "포인트 잔액이 부족합니다."),
    POINT_CONFLICT(HttpStatus.CONFLICT, "POINT_CONFLICT", "포인트 처리 중 충돌이 발생했습니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "ORDER_ALREADY_PAID", "이미 결제된 주문입니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS", "변경할 수 없는 주문 상태입니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS", "변경할 수 없는 결제 상태입니다."),

    // 500 INTERNAL_SERVER_ERROR
    DATA_PLATFORM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DATA_PLATFORM_SEND_FAILED", "주문 내역 전송에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return httpStatus.value();
    }
}
