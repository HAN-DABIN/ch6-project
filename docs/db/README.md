# DB Guide

이 문서는 커피숍 주문 시스템의 테이블 계약을 기록한다.
Agent는 Entity 또는 Repository를 수정하기 전에 이 문서를 먼저 확인한다.

## 테이블 목록

| 테이블 | 설명 |
| --- | --- |
| `users` | 사용자 정보 |
| `menus` | 커피 메뉴 정보 |
| `points` | 사용자 현재 포인트 잔액 |
| `point_histories` | 포인트 충전/사용 이력 |
| `orders` | 주문 정보 |
| `payments` | 결제 정보 |

## users

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 사용자 ID |
| `login_id` | VARCHAR | UNIQUE, NOT NULL | 로그인 ID |
| `password` | VARCHAR | NOT NULL | 암호화된 비밀번호 |
| `nickname` | VARCHAR | NOT NULL | 닉네임 |
| `created_at` | DATETIME | NOT NULL | 생성 일시 |
| `updated_at` | DATETIME | NOT NULL | 수정 일시 |

## menus

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 메뉴 ID |
| `name` | VARCHAR | NOT NULL | 메뉴명 |
| `price` | BIGINT | NOT NULL | 가격 |
| `status` | VARCHAR | NOT NULL | 메뉴 상태, `ACTIVE`, `INACTIVE` |
| `created_at` | DATETIME | NOT NULL | 생성 일시 |
| `updated_at` | DATETIME | NOT NULL | 수정 일시 |

## points

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 포인트 ID |
| `user_id` | BIGINT | FK, UNIQUE, NOT NULL | 사용자 ID |
| `balance` | BIGINT | NOT NULL | 현재 포인트 잔액 |

## point_histories

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 포인트 이력 ID |
| `user_id` | BIGINT | FK, NOT NULL | 사용자 ID |
| `amount` | BIGINT | NOT NULL | 변동 포인트 |
| `type` | VARCHAR | NOT NULL | `CHARGE`, `USE` |
| `balance_after` | BIGINT | NOT NULL | 변동 이후 잔액 |
| `created_at` | DATETIME | NOT NULL | 생성 일시 |

## orders

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 주문 ID |
| `user_id` | BIGINT | FK, NOT NULL | 사용자 ID |
| `menu_id` | BIGINT | FK, NOT NULL | 메뉴 ID |
| `order_price` | BIGINT | NOT NULL | 주문 당시 메뉴 가격 |
| `status` | VARCHAR | NOT NULL | 주문 상태 |
| `created_at` | DATETIME | NOT NULL | 생성 일시 |
| `updated_at` | DATETIME | NOT NULL | 수정 일시 |

## payments

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 결제 ID |
| `order_id` | BIGINT | FK, NOT NULL | 주문 ID |
| `user_id` | BIGINT | FK, NOT NULL | 사용자 ID |
| `amount` | BIGINT | NOT NULL | 결제 금액 |
| `status` | VARCHAR | NOT NULL | 결제 상태 |
| `created_at` | DATETIME | NOT NULL | 생성 일시 |
| `updated_at` | DATETIME | NOT NULL | 수정 일시 |

## 관계 규칙

- 사용자는 하나의 포인트 계좌를 가진다.
- 하나의 사용자는 여러 주문을 가질 수 있다.
- 하나의 주문은 하나의 결제를 가진다.
- 포인트 이력은 사용자 기준으로 기록한다.
- 주문과 결제 기록은 감사와 추적을 위해 사용자 삭제와 별도로 보존 정책을 고려한다.
