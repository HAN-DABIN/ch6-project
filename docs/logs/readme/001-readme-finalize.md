# 001-readme-finalize

## 요청

- 사용자 요청: `README.md`의 설계 내용, API 명세서, 설계 의도, 문제 해결 전략, 기술적 선택 이유를 현재 실제 코드 기준으로 보정하고 보강한다.
- 작업 의도: README가 실제 Controller, DTO, 공통 응답, 예외 코드, 보안 정책, 인기 메뉴 구현 구조와 어긋나지 않도록 정리한다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식:
  - README와 지정된 Controller, DTO, 공통 응답, 예외 코드, 보안 설정, 인기 메뉴 랭킹/Kafka 구현, 정책 문서를 먼저 읽었다.
  - 실제 Controller 경로와 DTO 필드명을 기준으로 API 명세를 보정했다.
  - 인기 메뉴 설명을 Redis ZSET, Kafka `PaymentCompletedEvent`, MySQL `findAllById` bulk 조회 구조로 수정했다.
  - README 필수 항목인 ERD, API 명세서, 설계 의도, 문제 해결 전략, 기술적 선택 이유, 동시성 처리, 데이터 일관성, 다중 서버/다중 인스턴스 고려, 테스트 결과, AI Agent Harness 도입 내용을 추가했다.
- 읽은 파일:
  - `AGENTS.md`
  - `README.md`
  - `docs/logs-guide.md`
  - `docs/api/README.md`
  - `docs/db/README.md`
  - `docs/policy/point-policy.md`
  - `docs/policy/order-payment-policy.md`
  - `docs/policy/popular-menu-policy.md`
  - `src/main/java/com/example/ch6project/domain/auth/controller/AuthController.java`
  - `src/main/java/com/example/ch6project/domain/menu/controller/MenuController.java`
  - `src/main/java/com/example/ch6project/domain/point/controller/PointController.java`
  - `src/main/java/com/example/ch6project/domain/order/controller/OrderController.java`
  - `src/main/java/com/example/ch6project/common/response/CommonApiResponse.java`
  - `src/main/java/com/example/ch6project/common/exception/ErrorCode.java`
  - `src/main/java/com/example/ch6project/common/exception/GlobalExceptionHandler.java`
  - `src/main/java/com/example/ch6project/common/config/SecurityConfig.java`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingConsumer.java`
  - `src/main/java/com/example/ch6project/domain/payment/event/PaymentCompletedProducer.java`
  - `src/main/java/com/example/ch6project/domain/payment/event/PaymentCompletedEvent.java`
  - `src/main/java/com/example/ch6project/domain/order/service/OrderService.java`
  - `src/main/java/com/example/ch6project/domain/point/service/PointService.java`
  - `src/main/java/com/example/ch6project/domain/point/repository/PointRepository.java`
  - `src/main/java/com/example/ch6project/domain/*/dto/*.java`
  - `src/main/java/com/example/ch6project/domain/*/entity/*.java`
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `build.gradle`
  - `src/test/resources/application-test.yml`
- 수정한 파일:
  - `README.md`
  - `docs/logs/readme/001-readme-finalize.md`

### 결과

- 실행한 검증:
  - `./gradlew test`
- 결과:
  - 샌드박스 내부 실행은 Gradle wrapper lock 파일 접근 중 `Operation not permitted`로 실패했다.
  - 샌드박스 외부 승인 실행은 `BUILD SUCCESSFUL in 684ms`로 성공했다.
  - Gradle task는 모두 `UP-TO-DATE`였다.

### 증거

- 테스트 명령:
  - `./gradlew test`
- 테스트 리포트:
  - `build/test-results/test/TEST-com.example.ch6project.Ch6ProjectApplicationTests.xml`
  - `build/test-results/test/TEST-com.example.ch6project.domain.order.service.OrderServiceTest.xml`
- 테스트 리포트 요약:
  - `Ch6ProjectApplicationTests`: 1 tests, 0 failures, 0 errors
  - `OrderServiceTest`: 1 tests, 0 failures, 0 errors

### 결정 사항

- 결정: README의 API 명세는 `docs/api/README.md`보다 실제 Controller와 DTO를 우선해 작성했다.
- 이유: 사용자 조건이 실제 코드와 문서가 다르면 실제 코드를 우선하라는 것이었고, `docs/api/README.md`에는 `/api/point/charge`처럼 실제 Controller와 다른 경로가 남아 있었기 때문이다.
- 결정: 포인트 충전과 주문 API Request Body에서 `userId`를 제거했다.
- 이유: 실제 Controller는 `@AuthenticationPrincipal Long userId`를 사용하고 요청 DTO에는 `userId` 필드가 없기 때문이다.
- 결정: 인기 메뉴 설계 설명을 Redis ZSET과 Kafka Consumer 기반 집계로 수정했다.
- 이유: 현재 구현은 매 요청마다 `orders` 원본을 직접 집계하지 않고, `PaymentCompletedEvent` 소비로 Redis 날짜별 ZSET을 갱신한 뒤 조회 시 최근 7일 score를 합산하기 때문이다.

### 남은 리스크

- 리스크: `docs/api/README.md`에는 아직 실제 코드와 다른 API 경로와 응답 예시가 남아 있다.
- 리스크: H2 기반 테스트 성공은 MySQL 실제 락 동작을 완전히 대체하지 못한다.
- 리스크: Kafka 이벤트 발행 실패가 주문 트랜잭션에 미치는 영향은 README에 리스크로 기록했지만, 정책/구현 정합성은 별도 작업이 필요하다.
- 다음 확인 사항:
  - `docs/api/README.md`를 실제 Controller/DTO 기준으로 별도 보정한다.
  - MySQL 또는 Testcontainers 기반 동시성 통합 테스트를 검토한다.
  - Kafka 이벤트 발행 실패 처리 정책을 구체화한다.

## API 문서 보정 - 2026-07-19

### 시도

- 접근 방식:
  - `README.md`, `docs/api/README.md`, 실제 Controller/DTO, `CommonApiResponse`, `ErrorCode`를 다시 읽었다.
  - `docs/api/README.md`의 API 경로, 인증 여부, Request Body, Response 예시를 실제 코드 기준으로 보정했다.
  - README의 `docs/api/README.md` 불일치 문장을 API 문서 보정 완료 내용으로 변경했다.
- 읽은 파일:
  - `README.md`
  - `docs/api/README.md`
  - `src/main/java/com/example/ch6project/domain/point/controller/PointController.java`
  - `src/main/java/com/example/ch6project/domain/order/controller/OrderController.java`
  - `src/main/java/com/example/ch6project/domain/menu/controller/MenuController.java`
  - `src/main/java/com/example/ch6project/common/response/CommonApiResponse.java`
  - `src/main/java/com/example/ch6project/common/exception/ErrorCode.java`
  - `src/main/java/com/example/ch6project/domain/point/dto/PointChargeRequest.java`
  - `src/main/java/com/example/ch6project/domain/point/dto/PointChargeResponse.java`
  - `src/main/java/com/example/ch6project/domain/order/dto/OrderRequest.java`
  - `src/main/java/com/example/ch6project/domain/order/dto/OrderResponse.java`
- 수정한 파일:
  - `docs/api/README.md`
  - `README.md`
  - `docs/logs/readme/001-readme-finalize.md`

### 결과

- 보정 내용:
  - 포인트 충전 경로를 `/api/point/charge`에서 실제 Controller 기준 `/api/points/charge`로 수정했다.
  - 포인트 충전과 주문 API는 JWT 인증 사용자 기준이므로 Request Body에서 `userId`를 받지 않는다고 명시했다.
  - 포인트 충전 응답을 실제 `PointController`와 `PointChargeResponse` 기준으로 `201`, `포인트가 충전되었습니다.`, `id`, `chargeAmount`, `balance`로 수정했다.
  - 주문 응답을 실제 `OrderController`와 `OrderResponse` 기준으로 `200`, `커피 주문 및 결제 성공`, `id`, `userId`, `menuId`, `orderPrice`, `paymentAmount`, `balance`, `orderStatus`, `paymentStatus`로 수정했다.
  - 메뉴 목록 응답을 실제 `GetMenuResponse` 기준으로 `id`, `name`, `price`로 수정했다.
  - 로그인 응답 메시지를 실제 `AuthController` 기준 `로그인을 성공했습니다.`로 수정했다.
  - 실패 응답 예시에 `code` 필드와 검증 실패 `data` 예시를 포함했다.
  - 주요 실패 응답 표를 `ErrorCode` 기준으로 추가했다.

### 검증

- 실행한 검증:
  - `git diff --check`
- 결과:
  - 통과.

### 남은 리스크

- 해소: 이전 Attempt에서 남긴 `docs/api/README.md`의 API 경로와 응답 예시 불일치 리스크는 이번 문서 보정으로 해소했다.
- 리스크: `SecurityConfig`에는 인증 경로 matcher가 `/api/point/**`로 되어 있어 실제 `PointController` 경로 `/api/points/**`와 다르다. 이번 작업은 문서 보정이므로 코드는 수정하지 않았다.
- 다음 확인 사항:
  - 별도 코드 작업에서 `SecurityConfig`의 포인트 인증 matcher를 실제 경로 기준으로 보정할지 검토한다.
