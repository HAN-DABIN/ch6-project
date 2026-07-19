# Order Concurrency Test Stabilize

## 상태

- 진행 상태: 계획
- 작성일: 2026-07-19
- 대상 기능: 주문/포인트 동시성 테스트 안정화

## 작업 배경

테스트 DB 설정 분리 작업 이후 `./gradlew test`에서 `${DB_URL}` 미치환, Hibernate Dialect 결정, JPA `entityManagerFactory`, ApplicationContext 로딩 문제는 해소됐다.
하지만 `OrderServiceTest.동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다()`는 여전히 실패한다.

현재 실패는 `successCount`가 expected `1`, actual `0`으로 나온다.
따라서 이번 작업은 동시 주문 테스트에서 왜 성공 카운트가 0이 되는지 원인을 관측 가능하게 만들고, 테스트 목적에 맞게 안정화하는 계획을 세우는 것이다.

## 현재 문제

- 테스트는 동일 사용자, 동일 메뉴, 포인트 5000P, 메뉴 가격 5000원 조건에서 2개 스레드가 동시에 주문을 요청한다.
- 기대 결과는 성공 1건, 실패 1건, 잔액 0, 주문 1건, 결제 1건이다.
- 실제 결과는 `OrderServiceTest.java:89`에서 `successCount`가 0이라 실패한다.
- 테스트 코드의 catch 블록은 `CustomException`만 잡아서 `failCount`를 증가시킨다.
- `orderService.order(...)`에서 `CustomException`이 아닌 예외가 발생하면 해당 스레드는 `successCount`도 `failCount`도 증가시키지 않고 `finally`에서 latch만 감소시킨다.
- 현재 테스트 리포트는 assertion 실패만 보여주며, 각 worker thread에서 발생한 실제 예외 타입과 메시지는 기록하지 않는다.

## 읽은 문서와 코드

- 문서:
  - `AGENTS.md`
  - `docs/workflow/plan-guide.md`
  - `docs/policy/order-payment-policy.md`
  - `docs/policy/point-policy.md`
  - `docs/logs/test/db-config/001-test-db-config.md`
- 코드:
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `src/main/java/com/example/ch6project/domain/order/service/OrderService.java`
  - `src/main/java/com/example/ch6project/domain/point/repository/PointRepository.java`
  - `src/main/java/com/example/ch6project/domain/payment/event/PaymentCompletedProducer.java`
  - `src/main/java/com/example/ch6project/common/config/KafkaConfig.java`
  - `src/main/java/com/example/ch6project/domain/point/entity/Point.java`
- 테스트 결과:
  - `build/test-results/test/TEST-com.example.ch6project.domain.order.service.OrderServiceTest.xml`

## 원인 후보

### 후보 1: worker thread에서 `CustomException`이 아닌 예외가 발생한다

- 근거:
  - 테스트는 `catch (CustomException e)`만 처리한다.
  - `RuntimeException`, Kafka 관련 예외, JPA/Pessimistic Lock 관련 예외, DataAccessException 등은 카운트되지 않는다.
  - 이 경우 `successCount`와 `failCount`가 모두 기대와 다르게 나올 수 있다.
- 먼저 확인할 것:
  - 테스트에서 `Throwable` 또는 `Exception`을 별도 컬렉션에 모아 실제 예외 타입과 메시지를 확인한다.
  - `Future<?>`를 보관하고 `future.get()`으로 worker thread 예외를 테스트 스레드에서 드러나게 한다.

### 후보 2: Kafka 이벤트 전송 의존성 때문에 성공 경로가 완료되지 못한다

- 근거:
  - `OrderService.order()`는 포인트 차감, 포인트 이력 저장, 주문 저장, 결제 저장 후 `paymentCompletedProducer.send(...)`를 호출한다.
  - `PaymentCompletedProducer.send()`는 `KafkaTemplate.send(...)`를 호출한다.
  - `KafkaConfig`는 producer bootstrap server를 `localhost:9092,localhost:9093,localhost:9094`로 코드에 직접 설정한다.
  - 실패 리포트에는 주문/결제 insert 이후 Kafka producer 설정 로그가 출력된다.
- 아직 확정할 수 없는 점:
  - 현재 리포트만으로 Kafka 전송이 실제 예외를 던졌는지는 확인되지 않는다.
  - `KafkaTemplate.send(...)`는 비동기 Future를 반환하므로 send 호출 자체가 항상 즉시 실패한다고 단정할 수 없다.
- 먼저 확인할 것:
  - `PaymentCompletedProducer`를 테스트 더블로 대체했을 때 성공/실패 카운트가 의도대로 나오는지 확인한다.
  - Kafka 전송 실패가 주문 트랜잭션에 영향을 줘야 하는지 정책을 재확인한다.

### 후보 3: H2의 `select ... for update` 동작이 MySQL과 달라 동시성 테스트가 불안정하다

- 근거:
  - `PointRepository.findByUserIdForUpdate()`는 `@Lock(LockModeType.PESSIMISTIC_WRITE)`와 JPQL query를 사용한다.
  - 테스트 리포트에는 H2에서 `select ... for update`가 실행된 로그가 있다.
  - `docs/logs/test/db-config/001-test-db-config.md`에서도 H2가 실제 MySQL의 락/트랜잭션 동작을 완전히 재현하지 않는 리스크를 기록했다.
- 아직 확정할 수 없는 점:
  - 현재 실패가 H2 락 차이 때문인지, Kafka/비동기 예외 때문인지는 분리되지 않았다.
- 먼저 확인할 것:
  - Kafka 의존을 제거한 뒤에도 H2에서 동시성 assertion이 실패하는지 확인한다.
  - 필요 시 MySQL 또는 Testcontainers 기반 통합 테스트로 포인트 락 동작을 따로 검증한다.

### 후보 4: 테스트가 실패 유형을 `CustomException` 하나로만 모델링해 관측성이 부족하다

- 근거:
  - 실패로 인정하는 예외가 `CustomException`뿐이다.
  - 동시성 테스트의 목적은 "한 요청만 성공하고 나머지는 잔액 부족 또는 제어된 실패가 된다"는 것인데, 현재 테스트는 예상 밖 예외를 별도 실패 원인으로 드러내지 않는다.
- 먼저 확인할 것:
  - 예상 실패 카운트와 예상 밖 예외 카운트를 분리한다.
  - 예상 밖 예외가 있으면 테스트가 즉시 실패하면서 예외 메시지를 보여주도록 수정한다.

## 변경 대상

- 변경 예상 파일:
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - 필요 시 `src/test/resources/application-test.yml`
  - 필요 시 테스트 전용 configuration 또는 mock 설정 파일
  - `docs/logs/order/payment/` 또는 `docs/logs/test/order/` 아래 작업 로그
- 변경하지 않을 파일:
  - 운영 datasource 설정
  - 운영 Kafka broker 설정
  - 주문/결제/포인트 도메인 로직
  - API 응답 DTO

## 접근 방향

1. `OrderServiceTest`에서 worker thread 내부 예외를 숨기지 않도록 관측성을 먼저 높인다.
   - `Future<?>` 목록을 저장하고 `future.get()`으로 예외를 확인한다.
   - 또는 예상 밖 예외를 `List<Throwable>`에 모은 뒤 assertion 전에 출력/검증한다.
2. `PaymentCompletedProducer`를 테스트에서 mock 또는 test double로 대체해 외부 Kafka 의존을 제거한다.
   - 우선 후보: `@MockitoBean PaymentCompletedProducer paymentCompletedProducer`
   - 목적: 주문/포인트 동시성 테스트가 Kafka broker 상태와 무관하게 동작하도록 한다.
3. Kafka 의존 제거 후에도 실패한다면 H2의 pessimistic lock 동작 후보를 검증한다.
   - H2 기반 테스트는 "서비스 흐름과 제어된 실패" 검증으로 제한한다.
   - 실제 MySQL 락 검증은 별도 MySQL/Testcontainers 통합 테스트 계획으로 분리한다.
4. 테스트 assertion을 더 명확히 한다.
   - 성공 1건
   - 제어된 실패 1건
   - 예상 밖 예외 0건
   - 잔액 0
   - 주문 1건
   - 결제 1건
5. 정책과 충돌하지 않도록 Kafka 이벤트 실패가 주문 트랜잭션에 미치는 영향은 이번 테스트 안정화 범위에서는 변경하지 않는다.
   - 정책 변경이 필요하면 별도 Plan으로 분리한다.

## 통과 기준

- `OrderServiceTest`가 실패할 때 worker thread의 실제 예외 타입과 메시지를 확인할 수 있다.
- 외부 Kafka broker 실행 여부와 무관하게 주문/포인트 동시성 테스트가 실행된다.
- 테스트가 성공 1건, 제어된 실패 1건, 예상 밖 예외 0건을 검증한다.
- 포인트 잔액은 0으로 남는다.
- 주문과 결제는 각각 1건만 저장된다.
- 테스트 안정화 과정에서 운영 Kafka 설정과 주문/결제/포인트 도메인 로직을 변경하지 않는다.
- `./gradlew test` 결과를 성공 또는 실패로 사실대로 기록한다.

## 테스트 계획

- 우선 `./gradlew test --tests com.example.ch6project.domain.order.service.OrderServiceTest`로 대상 테스트만 실행한다.
- 대상 테스트가 통과하면 `./gradlew test` 전체를 실행한다.
- 샌드박스에서 `.gradle` lock 권한 문제가 반복되면 승인 실행 결과와 구분해 기록한다.
- 실패 시 아래 정보를 로그에 남긴다.
  - worker thread 예외 타입
  - worker thread 예외 메시지
  - successCount
  - failCount
  - unexpectedExceptionCount
  - point balance
  - order count
  - payment count

## 남은 리스크

- Kafka producer를 mock 처리하면 실제 Kafka 전송 실패가 주문 트랜잭션에 미치는 영향은 검증하지 못한다.
- H2 기반 동시성 테스트가 통과해도 MySQL 운영 환경의 락 동작을 완전히 보장하지는 않는다.
- 실제 MySQL 동시성 검증은 Testcontainers 또는 별도 통합 테스트 환경이 필요할 수 있다.
- 현재 정책 문서에는 Kafka 이벤트 발행 실패가 주문/결제 트랜잭션에 어떤 영향을 줄지 명확히 기록해야 한다고 되어 있으나, 구현에서는 주문 트랜잭션 안에서 producer를 호출한다. 이 정책/구현 정합성은 별도 작업으로 다룰 필요가 있다.
