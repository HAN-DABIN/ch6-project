# k6 Load Test

## 상태

- 진행 상태: 계획
- 작성일: 2026-07-19
- 대상 기능: 메뉴 조회, 인기 메뉴 조회, 주문/결제 부하 테스트 방향

## 작업 배경

- 현재 프로젝트는 커피숍 주문 시스템으로 메뉴 조회, Redis ZSET 기반 인기 메뉴 조회, 포인트 기반 주문/결제를 제공한다.
- README와 API 문서 기준 주요 조회 API는 다음과 같다.
  - `GET /api/menus`
  - `GET /api/menus/popular`
  - 선택 대상: `POST /api/orders`
- 인기 메뉴는 현재 구현 기준으로 매 요청마다 `orders` 원본을 직접 집계하지 않는다.
  - 결제 완료 시 `PaymentCompletedEvent`가 Kafka topic `payment-completed`로 발행된다.
  - `MenuRankingConsumer`가 이벤트를 소비해 Redis 날짜별 ZSET `menu:ranking:{yyyy-MM-dd}`의 score를 증가시킨다.
  - `MenuRankingService.getPopularMenus()`는 최근 7일 Redis ZSET을 읽어 메뉴별 score를 합산하고, 상위 메뉴 ID의 상세 정보를 MySQL `findAllById` bulk 조회로 가져온다.
- 이번 작업은 k6 기반 부하 테스트 스크립트와 실행 문서를 추가하기 위한 계획이다.

## 왜 캐시 전/후 비교가 아니라 조회 전략 비교로 잡는지

- 현재 코드에는 “인기 메뉴를 MySQL 원본 집계로 조회하는 버전”과 “Redis ZSET으로 조회하는 버전”이 동시에 존재하지 않는다.
- 따라서 캐시 전/후 비교로 설계하면 존재하지 않는 구현을 기준선으로 삼게 되어 실제 코드 기준 문서화 원칙과 맞지 않는다.
- 이번 부하 테스트는 현재 존재하는 두 조회 전략의 응답 특성을 비교한다.
  - 일반 메뉴 조회: MySQL `menus` 전체 조회 후 DTO 변환
  - 인기 메뉴 조회: Redis ZSET 최근 7일 조회, score 합산, Top 3 메뉴 MySQL bulk 조회
- 비교 관점은 “캐시 도입 효과”가 아니라 다음 차이를 관찰하는 것이다.
  - 단일 MySQL read API와 Redis + MySQL 혼합 read API의 latency 차이
  - Redis ZSET key 수와 score 합산 과정이 p95/p99에 주는 영향
  - 인기 메뉴 데이터가 비어 있을 때와 채워져 있을 때의 응답 특성

## 읽은 문서와 코드

- 문서:
  - `AGENTS.md`
  - `docs/workflow/plan-guide.md`
  - `README.md`
  - `docs/api/README.md`
  - `docs/policy/popular-menu-policy.md`
  - `docs/logs-guide.md`
- 코드:
  - `src/main/java/com/example/ch6project/domain/menu/controller/MenuController.java`
  - `src/main/java/com/example/ch6project/domain/order/controller/OrderController.java`
  - `src/main/java/com/example/ch6project/domain/menu/service/MenuService.java`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`

## 테스트 대상 API

### 1. 메뉴 목록 조회

- Method: `GET`
- Path: `/api/menus`
- 인증: 불필요
- 실제 Controller: `MenuController.getMenus()`
- 응답 메시지: `메뉴 조회 성공`
- 주요 의존:
  - MySQL
  - `MenuRepository.findAll()`

### 2. 인기 메뉴 조회

- Method: `GET`
- Path: `/api/menus/popular`
- 인증: 불필요
- 실제 Controller: `MenuController.getPopularMenus()`
- 응답 메시지: `인기 메뉴 조회 성공`
- 주요 의존:
  - Redis ZSET
  - 최근 7일 key `menu:ranking:{date}`
  - MySQL `MenuRepository.findAllById(...)`

### 3. 주문/결제 부하 테스트 방향

- Method: `POST`
- Path: `/api/orders`
- 인증: 필요
- 실제 Controller: `OrderController.order(...)`
- Request Body:

```json
{
  "menuId": 1
}
```

- 이번 Generate 단계에서는 선택적 방향 정리 대상으로 둔다.
- 실제 주문 부하 테스트는 사용자/포인트/메뉴 seed, JWT 발급, 포인트 잔액, Kafka/Redis/MySQL 상태를 함께 준비해야 하므로 조회 테스트와 분리하는 것이 안전하다.

## k6 스크립트 파일 위치 제안

- 조회 비교 스크립트:
  - `load-tests/k6/menu-read-comparison.js`
- 실행 문서:
  - `docs/load-test/k6.md`
- 선택적 주문/결제 방향 문서 또는 후속 스크립트 후보:
  - `load-tests/k6/order-payment-smoke.js`
  - 단, 이번 범위에서는 주문/결제 스크립트는 바로 확정하지 않고 실행 방향과 필요한 사전 조건을 문서화한다.
- 작업 로그 후보:
  - `docs/logs/load-test/k6/001-menu-read-comparison.md`

## 시나리오 설계

### 공통 설정

- 기본 URL은 환경 변수로 주입한다.
  - 예: `BASE_URL=http://localhost:8080`
- 응답 검증은 HTTP status와 공통 응답 body를 함께 확인한다.
  - `status === 200`
  - `json.status === 200`
  - 메뉴 조회: `json.message === "메뉴 조회 성공"`
  - 인기 메뉴 조회: `json.message === "인기 메뉴 조회 성공"`
- 테스트 대상 API별 태그를 부여한다.
  - `api: menus`
  - `api: popular-menus`

### 시나리오 A: 메뉴 조회 단독

- 목적: MySQL 메뉴 조회 API의 기본 응답 특성을 측정한다.
- 대상: `GET /api/menus`
- 부하 형태:
  - smoke: 낮은 VU로 스크립트와 응답 계약 확인
  - load: 일정 VU로 p95, p99, 실패율 확인

### 시나리오 B: 인기 메뉴 조회 단독

- 목적: Redis ZSET 최근 7일 조회 + MySQL bulk 조회 API의 응답 특성을 측정한다.
- 대상: `GET /api/menus/popular`
- 부하 형태:
  - smoke: Redis 데이터가 비어 있거나 채워진 상태 모두 응답 계약 확인
  - load: 일정 VU로 p95, p99, 실패율 확인
- 사전 조건:
  - Redis가 실행 중이어야 한다.
  - 인기 메뉴 데이터가 있는 상태를 보려면 `menu:ranking:{date}` key에 메뉴 ID와 score가 준비되어야 한다.

### 시나리오 C: 조회 혼합

- 목적: 실제 사용자 트래픽처럼 일반 메뉴 조회와 인기 메뉴 조회가 섞일 때의 응답 특성을 비교한다.
- 대상:
  - `GET /api/menus`
  - `GET /api/menus/popular`
- 부하 형태:
  - k6 `scenarios` 또는 요청 가중치를 사용한다.
  - 예: 메뉴 조회 70%, 인기 메뉴 조회 30%

### 선택 시나리오 D: 주문/결제 방향

- 목적: 주문 API 부하 테스트 설계를 위한 사전 조건을 정리한다.
- 대상: `POST /api/orders`
- 필요한 준비:
  - 로그인 또는 사전 발급 JWT
  - 충분한 포인트가 있는 테스트 사용자 풀
  - 활성 메뉴 ID
  - Kafka broker
  - Redis
  - MySQL
- 주의:
  - 같은 사용자를 반복 사용하면 포인트 차감과 비관적 락 때문에 의도하지 않은 병목이 생긴다.
  - 주문 부하 테스트는 “정상 주문 처리량”과 “동일 사용자 동시 주문 락 경합”을 분리해야 한다.

## 측정 지표

- k6 기본 지표:
  - `http_req_duration`
  - `http_req_failed`
  - `http_reqs`
  - `iterations`
  - `vus`
  - `checks`
- 비교할 핵심 지표:
  - API별 평균 latency
  - API별 p95 latency
  - API별 p99 latency
  - API별 실패율
  - 처리량, requests per second
- 태그 기반 비교:
  - `http_req_duration{api:menus}`
  - `http_req_duration{api:popular-menus}`
  - `http_req_failed{api:menus}`
  - `http_req_failed{api:popular-menus}`

## 통과 기준

- k6 스크립트가 `BASE_URL` 환경 변수로 대상 서버를 바꿔 실행될 수 있다.
- `GET /api/menus`와 `GET /api/menus/popular` 모두 실제 API 경로와 응답 메시지를 기준으로 check를 수행한다.
- 스크립트 실행 문서에 아래 내용이 포함된다.
  - 사전 준비
  - 실행 명령
  - 환경 변수
  - 결과 해석 방법
  - Redis 인기 메뉴 데이터 준비 방법 또는 미준비 시 기대 결과
- 기본 smoke 실행에서 HTTP 실패율이 0이어야 한다.
- load 실행 기준은 초안으로 다음을 제안한다.
  - `http_req_failed < 1%`
  - `checks > 99%`
  - p95 목표값은 로컬 장비, Docker 상태, DB/Redis seed 크기에 따라 달라지므로 최초 실행 결과를 기준선으로 기록한다.
- 주문/결제 부하 테스트는 이번 범위에서 실행 가능 스크립트가 아니라 후속 작업 방향으로 정리되어도 통과로 본다.

## 실행 방법

Generate 단계에서 실행 문서를 추가할 때 다음 형태를 제안한다.

```bash
BASE_URL=http://localhost:8080 k6 run load-tests/k6/menu-read-comparison.js
```

옵션 예시:

```bash
BASE_URL=http://localhost:8080 \
K6_SCENARIO=smoke \
k6 run load-tests/k6/menu-read-comparison.js
```

사전 준비:

1. 애플리케이션을 실행한다.
   - `./gradlew bootRun`
2. MySQL, Redis, Kafka가 필요한 프로파일과 설정으로 실행 중인지 확인한다.
3. 메뉴 조회 테스트를 위해 `menus` 데이터가 준비되어 있는지 확인한다.
4. 인기 메뉴 조회 테스트에서 랭킹 데이터가 있는 상태를 측정하려면 Redis ZSET `menu:ranking:{date}`에 테스트 데이터를 준비한다.

## 영향 범위

- 변경 예상 파일:
  - `load-tests/k6/menu-read-comparison.js`
  - `docs/load-test/k6.md`
  - `docs/logs/load-test/k6/001-menu-read-comparison.md`
- 영향받는 API:
  - `GET /api/menus`
  - `GET /api/menus/popular`
  - 선택 검토: `POST /api/orders`
- 영향받는 테이블/외부 시스템:
  - MySQL `menus`
  - Redis ZSET `menu:ranking:{date}`
  - 선택 검토 시 MySQL `users`, `points`, `orders`, `payments`, `point_histories`
  - 선택 검토 시 Kafka topic `payment-completed`

## 남은 질문 또는 리스크

- k6가 로컬에 설치되어 있는지 아직 확인하지 않았다.
- 프로젝트에 Docker Compose 또는 seed 자동화가 있는지 아직 확정하지 않았다.
- 메뉴 데이터와 Redis 인기 메뉴 데이터가 없는 환경에서는 응답은 성공해도 비교 의미가 약해질 수 있다.
- Redis 인기 메뉴 데이터 준비를 k6 스크립트에 포함할지, 별도 seed 절차로 문서화할지 Generate 단계에서 결정이 필요하다.
- `POST /api/orders`는 인증, 포인트 잔액, Kafka, Redis, MySQL 상태에 영향을 받으므로 조회 API와 같은 스크립트에 섞으면 원인 분석이 어려울 수 있다.
- `SecurityConfig`의 포인트 인증 matcher는 실제 `/api/points/**`와 다르게 `/api/point/**`로 되어 있으나, 이번 작업 대상은 메뉴 조회와 인기 메뉴 조회이며 코드는 수정하지 않는다.
