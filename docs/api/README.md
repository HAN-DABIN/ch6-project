# API Guide

이 문서는 커피숍 주문 시스템의 API 계약을 기록한다.
Agent는 Controller 또는 DTO를 수정하기 전에 이 문서를 먼저 확인한다.

## 공통 응답

### 성공 응답

```json
{
  "status": 200,
  "message": "요청 성공",
  "data": {}
}
```

### 실패 응답

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "입력값이 올바르지 않습니다.",
  "data": [
    "amount: 충전 금액은 0보다 커야 합니다."
  ]
}
```

검증 실패처럼 상세 정보가 있는 경우 `data`에 오류 목록이 포함될 수 있다.

## API 목록

| 기능 | Method | URL | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| 회원가입 | POST | `/api/auth/signup` | 불필요 | 사용자를 생성한다 |
| 로그인 | POST | `/api/auth/login` | 불필요 | JWT 토큰을 발급한다 |
| 메뉴 목록 조회 | GET | `/api/menus` | 불필요 | 메뉴 목록을 조회한다 |
| 인기 메뉴 조회 | GET | `/api/menus/popular` | 불필요 | 최근 7일 인기 메뉴 Top 3를 조회한다 |
| 포인트 충전 | POST | `/api/points/charge` | 필요 | 로그인 사용자의 포인트를 충전한다 |
| 커피 주문/결제 | POST | `/api/orders` | 필요 | 로그인 사용자가 메뉴를 주문하고 포인트로 결제한다 |

## 회원가입

`POST /api/auth/signup`

- 설명: 사용자를 생성한다.
- 인증: 불필요

### Request Body

```json
{
  "loginId": "test123",
  "nickname": "테스트",
  "password": "test1234"
}
```

### Response 201

```json
{
  "status": 201,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "loginId": "test123",
    "nickname": "테스트"
  }
}
```

## 로그인

`POST /api/auth/login`

- 설명: 로그인 ID와 비밀번호로 인증 후 JWT 토큰을 발급한다.
- 인증: 불필요

### Request Body

```json
{
  "loginId": "test123",
  "password": "test1234"
}
```

### Response 200

```json
{
  "status": 200,
  "message": "로그인을 성공했습니다.",
  "data": {
    "accessToken": "jwt-token"
  }
}
```

## 메뉴 목록 조회

`GET /api/menus`

- 설명: 커피 메뉴 목록을 조회한다.
- 인증: 불필요

### Response 200

```json
{
  "status": 200,
  "message": "메뉴 조회 성공",
  "data": [
    {
      "id": 1,
      "name": "아메리카노",
      "price": 4500
    }
  ]
}
```

## 포인트 충전

`POST /api/points/charge`

- 설명: 로그인 사용자의 포인트를 충전한다.
- 인증: 필요
- 사용자 ID: JWT 인증 사용자 기준
- Request Body에서 `userId`를 받지 않는다.

### Request Body

```json
{
  "amount": 10000
}
```

### Response 201

```json
{
  "status": 201,
  "message": "포인트가 충전되었습니다.",
  "data": {
    "id": 1,
    "chargeAmount": 10000,
    "balance": 15000
  }
}
```

## 커피 주문/결제

`POST /api/orders`

- 설명: 메뉴를 주문하고 포인트로 결제한다.
- 인증: 필요
- 사용자 ID: JWT 인증 사용자 기준
- 주문 금액은 서버에 저장된 메뉴 가격을 기준으로 한다.
- Request Body에서 `userId`를 받지 않는다.

### Request Body

```json
{
  "menuId": 2
}
```

### Response 200

```json
{
  "status": 200,
  "message": "커피 주문 및 결제 성공",
  "data": {
    "id": 10,
    "userId": 1,
    "menuId": 2,
    "orderPrice": 5000,
    "paymentAmount": 5000,
    "balance": 10000,
    "orderStatus": "COMPLETED",
    "paymentStatus": "COMPLETED"
  }
}
```

## 인기 메뉴 조회

`GET /api/menus/popular`

- 설명: 최근 7일간 인기 메뉴 Top 3를 조회한다.
- 인증: 불필요

### Response 200

```json
{
  "status": 200,
  "message": "인기 메뉴 조회 성공",
  "data": [
    {
      "menuId": 1,
      "name": "아메리카노",
      "price": 4500,
      "orderCount": 12
    }
  ]
}
```

### 인기 메뉴가 없는 경우

```json
{
  "status": 200,
  "message": "인기 메뉴 조회 성공",
  "data": []
}
```

## 주요 실패 응답

| HTTP Status | Code | Message |
| --- | --- | --- |
| 400 | `VALIDATION_FAILED` | 입력값이 올바르지 않습니다. |
| 400 | `INVALID_REQUEST` | 잘못된 요청입니다. |
| 400 | `INVALID_CHARGE_AMOUNT` | 충전 금액은 0보다 커야 합니다. |
| 400 | `INVALID_MENU_PRICE` | 메뉴 가격이 올바르지 않습니다. |
| 400 | `INVALID_ORDER_AMOUNT` | 주문 금액이 올바르지 않습니다. |
| 401 | `UNAUTHORIZED` | 로그인이 필요합니다. |
| 401 | `INVALID_CREDENTIALS` | 아이디 또는 비밀번호가 올바르지 않습니다. |
| 401 | `INVALID_TOKEN` | 유효하지 않은 토큰입니다. |
| 401 | `EXPIRED_TOKEN` | 만료된 토큰입니다. |
| 404 | `USER_NOT_FOUND` | 사용자를 찾을 수 없습니다. |
| 404 | `MENU_NOT_FOUND` | 메뉴를 찾을 수 없습니다. |
| 404 | `POINT_NOT_FOUND` | 포인트 정보를 찾을 수 없습니다. |
| 404 | `ORDER_NOT_FOUND` | 주문을 찾을 수 없습니다. |
| 404 | `PAYMENT_NOT_FOUND` | 결제 정보를 찾을 수 없습니다. |
| 409 | `LOGIN_ID_DUPLICATE` | 이미 사용 중인 ID입니다. |
| 409 | `MENU_INACTIVE` | 주문할 수 없는 메뉴입니다. |
| 409 | `INSUFFICIENT_POINT` | 포인트 잔액이 부족합니다. |
| 409 | `POINT_CONFLICT` | 포인트 처리 중 충돌이 발생했습니다. |
| 409 | `ORDER_ALREADY_PAID` | 이미 결제된 주문입니다. |
| 409 | `INVALID_ORDER_STATUS` | 변경할 수 없는 주문 상태입니다. |
| 409 | `INVALID_PAYMENT_STATUS` | 변경할 수 없는 결제 상태입니다. |
| 500 | `DATA_PLATFORM_SEND_FAILED` | 주문 내역 전송에 실패했습니다. |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다. |
