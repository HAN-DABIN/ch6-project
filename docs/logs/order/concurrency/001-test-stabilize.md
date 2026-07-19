# 001-test-stabilize

## 요청

- 사용자 요청: `OrderServiceTest.동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다()` 테스트의 관측성을 높이고, Kafka 외부 의존이 실패 원인으로 확인되거나 가능성이 높으면 테스트 더블로 대체한다.
- 작업 의도: 주문/포인트 동시성 테스트가 실패할 때 worker thread 예외 원인을 확인할 수 있게 만들고, 테스트 목적과 무관한 Kafka broker 의존을 제거해 안정화한다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식:
  - worker thread에서 `CustomException` 외 예외가 발생하면 `unexpectedExceptions`에 수집하도록 했다.
  - assertion 메시지에 `successCount`, `failCount`, `unexpectedExceptionCount`, `balance`, `orderCount`, `paymentCount`, 예외 타입/메시지를 포함하도록 했다.
  - `PaymentCompletedProducer`를 `@MockitoBean`으로 대체해 주문 성공 경로에서 외부 Kafka broker에 의존하지 않도록 했다.
- 읽은 파일:
  - `AGENTS.md`
  - `docs/workflow/generate-guide.md`
  - `docs/dev/ongoing/order-concurrency-test-stabilize.md`
  - `docs/logs/test/db-config/001-test-db-config.md`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `src/main/java/com/example/ch6project/domain/order/service/OrderService.java`
  - `src/main/java/com/example/ch6project/domain/point/repository/PointRepository.java`
  - `src/main/java/com/example/ch6project/domain/payment/event/PaymentCompletedProducer.java`
  - `src/main/java/com/example/ch6project/common/config/KafkaConfig.java`
- 수정한 파일:
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `docs/logs/order/concurrency/001-test-stabilize.md`

### 결과

- 실행한 검증:
  - `./gradlew test --tests "*OrderServiceTest*"`
- 결과:
  - 샌드박스 내부 실행은 `/Users/handabin/.gradle/wrapper/dists/.../gradle-9.5.1-bin.zip.lck` 접근 중 `Operation not permitted`로 Gradle이 시작되지 못했다.
  - 샌드박스 외부 승인 실행은 성공했다.
  - `OrderServiceTest`: 1 tests, 0 failures, 0 errors

### 증거

- 테스트 명령:
  - `./gradlew test --tests "*OrderServiceTest*"`
- 테스트 리포트:
  - `build/test-results/test/TEST-com.example.ch6project.domain.order.service.OrderServiceTest.xml`
- 테스트 리포트 요약:
  - tests: 1
  - failures: 0
  - errors: 0
  - active profile: `test`
  - datasource: `jdbc:h2:mem:ch6_project_test`

### 결정 사항

- 결정: `OrderServiceTest`에서 `PaymentCompletedProducer`를 `@MockitoBean`으로 대체했다.
- 이유: 이전 실패 로그에서 주문/결제 insert 이후 Kafka producer 설정 로그가 출력되었고, 이 테스트의 목적은 Kafka 전송이 아니라 포인트 차감 동시성 검증이기 때문이다. Kafka broker 의존을 제거하자 대상 테스트가 통과했다.
- 결정: 운영 datasource, 운영 Kafka 설정, `OrderService` 도메인 로직은 수정하지 않았다.
- 이유: 사용자 조건과 계획 범위가 테스트 안정화에 한정되어 있기 때문이다.
- 결정: 예상 밖 예외를 별도로 수집하고 assertion 메시지에 포함했다.
- 이유: 기존 테스트는 `CustomException`만 카운트해 worker thread에서 다른 예외가 발생해도 원인을 알기 어려웠기 때문이다.

### 남은 리스크

- 리스크: Kafka producer를 mock 처리했으므로 실제 Kafka 전송 실패가 주문 트랜잭션에 미치는 영향은 검증하지 않는다.
- 리스크: H2 기반 테스트가 통과했지만, H2의 `select ... for update` 동작은 MySQL의 락/트랜잭션 동작을 완전히 보장하지 않는다.
- 리스크: 전체 `./gradlew test`는 이번 Attempt에서 실행하지 않았다.
- 다음 확인 사항:
  - `./gradlew test` 전체 실행으로 다른 테스트 영향 여부를 확인한다.
  - MySQL 또는 Testcontainers 기반 포인트 동시성 통합 테스트를 별도 작업으로 검토한다.
  - Kafka 이벤트 발행 실패가 주문/결제 트랜잭션에 미치는 영향을 정책과 구현 관점에서 별도 정리한다.

## Evaluate 기록 - 2026-07-19

### 검증 대상

- 계획 문서:
  - `docs/dev/ongoing/order-concurrency-test-stabilize.md`
- 구현 파일:
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
- 로그 파일:
  - `docs/logs/order/concurrency/001-test-stabilize.md`

### 계획 대비 구현 확인

- 일치한 항목:
  - worker thread에서 `CustomException` 외 예외를 `unexpectedExceptions`에 수집한다.
  - assertion 실패 메시지에 `successCount`, `failCount`, `unexpectedExceptionCount`, `balance`, `orderCount`, `paymentCount`, 예외 타입/메시지를 포함한다.
  - `PaymentCompletedProducer`를 `@MockitoBean`으로 대체해 Kafka broker 외부 의존을 제거했다.
  - 운영 datasource 설정, Kafka 운영 설정, `OrderService` 도메인 로직은 수정하지 않았다.
- 계획 범위 밖 변경:
  - 확인된 범위에서는 없음.

### 실행한 명령

- `./gradlew test --tests "*OrderServiceTest*"`

### 결과

- 샌드박스 내부 실행:
  - Gradle wrapper lock 파일 접근 중 `Operation not permitted`로 Gradle이 시작되지 못했다.
- 샌드박스 외부 승인 실행:
  - 성공.
  - `OrderServiceTest`: 1 tests, 0 failures, 0 errors.
  - 테스트 리포트: `build/test-results/test/TEST-com.example.ch6project.domain.order.service.OrderServiceTest.xml`

### 통과 여부

- 대상 테스트 기준: 통과.
- 전체 `./gradlew test` 기준: 이번 Evaluate 단계에서는 실행하지 않아 통과로 표시하지 않는다.
- 판단:
  - 기존 `successCount expected 1, actual 0` 실패는 대상 테스트 재실행 기준으로 재현되지 않았다.
  - 테스트 실패 시 worker thread의 예상 밖 예외 타입과 메시지를 확인할 수 있도록 관측성이 개선됐다.
  - Kafka broker 의존은 `PaymentCompletedProducer` mock 처리로 대상 테스트 범위에서 제거됐다.

### 남은 리스크

- H2 기반 동시성 테스트는 MySQL 실제 락 동작을 완전히 대체하지 못한다.
  - 특히 `select ... for update`, 트랜잭션 격리 수준, lock wait 동작은 MySQL/Testcontainers 기반 통합 테스트로 별도 검증하는 것이 안전하다.
- `PaymentCompletedProducer`를 mock 처리했으므로 실제 Kafka 전송 실패가 주문/결제 트랜잭션에 미치는 영향은 검증하지 않는다.
- 전체 `./gradlew test`를 이번 Evaluate 단계에서 실행하지 않았으므로 다른 테스트와의 회귀 여부는 별도 확인이 필요하다.

### 다음 확인 사항

- 필요 시 `./gradlew test` 전체 실행으로 회귀 여부를 확인한다.
- MySQL 또는 Testcontainers 기반 포인트 동시성 통합 테스트를 별도 작업으로 계획한다.
- Kafka 이벤트 발행 실패 정책과 현재 트랜잭션 내 producer 호출의 정합성을 별도 작업으로 점검한다.

### Evaluate 재확인

- 실행한 명령:
  - `./gradlew test --tests "*OrderServiceTest*"`
- 결과:
  - 샌드박스 내부 실행은 Gradle wrapper lock 파일 접근 중 `Operation not permitted`로 실패했다.
  - 샌드박스 외부 승인 실행은 `BUILD SUCCESSFUL`로 종료됐다.
  - Gradle task 상태는 `UP-TO-DATE`였으므로 테스트 리포트의 직전 성공 결과를 유지한다.
- 전체 테스트:
  - 이번 Evaluate 단계에서는 전체 `./gradlew test`를 실행하지 않았다.
  - 대상 변경 범위가 `OrderServiceTest` 안정화에 한정되어 있고, 사용자 요청의 기준 명령인 `./gradlew test --tests "*OrderServiceTest*"`가 성공했기 때문이다.
