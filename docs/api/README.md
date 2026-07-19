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
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

## API 목록

| 기능 | Method | URL | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| 회원가입 | POST | `/api/auth/signup` | 불필요 | 사용자를 생성한다 |
| 로그인 | POST | `/api/auth/login` | 불필요 | JWT 토큰을 발급한다 |
| 메뉴 목록 조회 | GET | `/api/menus` | 불필요 | 판매 메뉴 목록을 조회한다 |
| 포인트 충전 | POST | `/api/point/charge` | 필요 | 로그인 사용자의 포인트를 충전한다 |
| 커피 주문/결제 | POST | `/api/orders` | 필요 | 메뉴를 주문하고 포인트로 결제한다 |
| 인기 메뉴 조회 | GET | `/api/menus/popular` | 불필요 | 최근 7일 인기 메뉴 Top 3를 조회한다 |

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
  "message": "로그인이 완료되었습니다.",
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
  "message": "메뉴 목록 조회 성공",
  "data": [
    {
      "menuId": 1,
      "name": "아메리카노",
      "price": 4500
    }
  ]
}
```

## 포인트 충전

`POST /api/point/charge`

- 설명: 로그인 사용자의 포인트를 충전한다.
- 인증: 필요

### Request Body

```json
{
  "amount": 10000
}
```

### Response 200

```json
{
  "status": 200,
  "message": "포인트 충전 성공",
  "data": {
    "userId": 1,
    "chargedAmount": 10000,
    "balance": 10000
  }
}
```

## 커피 주문/결제

`POST /api/orders`

- 설명: 메뉴를 주문하고 포인트로 결제한다.
- 인증: 필요

### Request Body

```json
{
  "menuId": 1
}
```

### Response 201

```json
{
  "status": 201,
  "message": "주문 결제 성공",
  "data": {
    "orderId": 1,
    "paymentId": 1,
    "menuId": 1,
    "orderPrice": 4500,
    "remainingPoint": 5500
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
