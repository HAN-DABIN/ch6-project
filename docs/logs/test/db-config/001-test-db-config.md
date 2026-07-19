# 001-test-db-config

## 요청

- 사용자 요청: 테스트 실행 시 `${DB_URL}`이 실제 JDBC URL로 치환되지 않아 JPA `entityManagerFactory` 생성과 Hibernate Dialect 결정이 실패하는 문제를 테스트 전용 설정으로 해결한다.
- 작업 의도: 운영 `application.yml`을 수정하지 않고, Spring Boot 테스트가 test profile과 H2 datasource를 사용하도록 분리한다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식: `src/test/resources/application-test.yml`을 추가해 H2 in-memory datasource를 설정하고, `build.gradle`에 H2 테스트 런타임 의존성을 추가했다. `@SpringBootTest` 기반 테스트 클래스에는 `@ActiveProfiles("test")`를 추가해 테스트 전용 설정을 활성화했다.
- 읽은 파일:
  - `AGENTS.md`
  - `docs/workflow/generate-guide.md`
  - `docs/dev/ongoing/test-db-config.md`
  - `docs/logs-guide.md`
  - `build.gradle`
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `src/main/java/com/example/ch6project/common/config/KafkaConfig.java`
  - `src/main/java/com/example/ch6project/domain/order/service/OrderService.java`
  - `src/main/java/com/example/ch6project/domain/payment/event/PaymentCompletedProducer.java`
- 수정한 파일:
  - `build.gradle`
  - `src/test/resources/application-test.yml`
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
  - `docs/logs/test/db-config/001-test-db-config.md`

### 결과

- 실행한 검증: `./gradlew test`
- 결과: 실패
- 실패 원인:
  - 샌드박스 내부 실행은 `/Users/handabin/.gradle/wrapper/dists/.../gradle-9.5.1-bin.zip.lck` 접근 중 `Operation not permitted`로 Gradle이 시작되지 못했다.
  - 샌드박스 외부 승인 실행은 Gradle이 시작되어 `compileJava`, `compileTestJava`, `processTestResources`까지 진행됐다.
  - `Ch6ProjectApplicationTests.contextLoads()`는 성공했다.
  - `OrderServiceTest.동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다()`는 실패했다. 실패 지점은 `successCount`가 1이어야 한다는 assertion이며, 실제 값은 0이었다.

### 증거

- 테스트 명령:
  - `./gradlew test`
- 테스트 결과 요약:
  - `Ch6ProjectApplicationTests`: 1 tests, 0 failures
  - `OrderServiceTest`: 1 tests, 1 failures
- 테스트 리포트 핵심:
  - test profile 활성화 확인: `The following 1 profile is active: "test"`
  - H2 datasource 사용 확인: `Database JDBC URL [jdbc:h2:mem:ch6_project_test]`
  - Hibernate Dialect 확인: `Database dialect: H2Dialect`
  - 실패 assertion: `expected: 1 but was: 0` at `OrderServiceTest.java:89`

### 결정 사항

- 결정: 운영 설정인 `src/main/resources/application.yml`은 수정하지 않았다.
- 이유: 사용자 조건과 계획 문서에서 운영 설정을 변경하지 않기로 했기 때문이다.
- 결정: 테스트 전용 datasource는 `application-test.yml`에 분리했다.
- 이유: 테스트가 `${DB_URL}` 환경 변수나 실제 MySQL 실행 여부에 의존하지 않도록 하기 위해서다.
- 결정: Kafka 설정 또는 도메인 서비스 구현은 수정하지 않았다.
- 이유: 이번 계획 범위는 테스트 DB 설정이며, Kafka 구조 변경은 계획에 없는 변경이기 때문이다.

### 남은 리스크

- 리스크: DB 설정 문제는 해소되어 `contextLoads()`가 성공했지만, 전체 `./gradlew test`는 아직 실패한다.
- 리스크: `OrderServiceTest`는 주문 성공 후 `PaymentCompletedProducer`를 통해 Kafka 전송을 수행한다. 현재 `KafkaConfig`는 `localhost:9092,localhost:9093,localhost:9094`를 코드에 직접 사용하므로, 테스트 환경에서도 외부 Kafka 의존이 남아 있다.
- 리스크: H2는 실제 MySQL의 락/동시성 동작을 완전히 재현하지 않으므로 포인트 중복 차감 방지 검증에는 한계가 있다.
- 다음 확인 사항:
  - `OrderServiceTest`에서 Kafka producer를 테스트 더블로 대체할지 별도 계획을 세운다.
  - 동시성 검증을 MySQL 기반 통합 테스트로 분리할지 검토한다.
  - `KafkaConfig`가 테스트 profile에서 외부 브로커에 의존하지 않도록 설정 분리하는 작업을 별도로 계획한다.

## Evaluate 기록 - 2026-07-19

### 평가 대상

- 계획 문서: `docs/dev/ongoing/test-db-config.md`
- 구현 파일:
  - `build.gradle`
  - `src/test/resources/application-test.yml`
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
- 로그 파일: `docs/logs/test/db-config/001-test-db-config.md`

### 계획 대비 구현 확인

- 일치: 운영 설정 `src/main/resources/application.yml`은 수정하지 않았다.
- 일치: 테스트 전용 설정 파일 `src/test/resources/application-test.yml`을 추가했다.
- 일치: `build.gradle`에 테스트 런타임용 H2 의존성 `testRuntimeOnly 'com.h2database:h2'`를 추가했다.
- 일치: `Ch6ProjectApplicationTests`와 `OrderServiceTest`에 `@ActiveProfiles("test")`를 추가해 test profile을 활성화했다.
- 일치: 도메인 Service/Repository/Entity, API 응답 DTO, 운영 Redis/Kafka 설정은 변경하지 않았다.

### `./gradlew test` 기준 평가

- 샌드박스 내부 실행:
  - 결과: 실패
  - 원인: `/Users/handabin/.gradle/wrapper/dists/.../gradle-9.5.1-bin.zip.lck` 접근 중 `Operation not permitted`로 Gradle wrapper가 시작되지 못했다.
- 샌드박스 외부 승인 실행:
  - 결과: 실패
  - 실행된 주요 task: `compileJava`, `processResources`, `classes`, `compileTestJava`, `processTestResources`, `testClasses`, `test`
  - `Ch6ProjectApplicationTests.contextLoads()` 결과: 성공
  - `OrderServiceTest.동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다()` 결과: 실패
  - 실패 assertion: `expected: 1 but was: 0` at `OrderServiceTest.java:89`

### DB 설정 문제 분리 판단

- `DB_URL` 치환 문제: 해결됨
  - 테스트 로그에서 datasource URL이 `${DB_URL}` 문자열이 아니라 `jdbc:h2:mem:ch6_project_test`로 확인됐다.
- Hibernate Dialect 결정 문제: 해결됨
  - 테스트 로그에서 `Database dialect: H2Dialect`로 확인됐다.
- JPA `entityManagerFactory` 생성 문제: 해결됨
  - 테스트 로그에서 `Initialized JPA EntityManagerFactory for persistence unit 'default'`로 확인됐다.
- ApplicationContext 로딩 문제: 해결됨
  - `Ch6ProjectApplicationTests.contextLoads()`가 성공했고, test profile 활성화 로그도 확인됐다.

### 남은 `OrderServiceTest` 실패 판단

- 이번 작업 범위 안의 실패 여부: 범위 밖
- 이유:
  - 이번 작업의 목적은 `${DB_URL}` 미치환으로 인한 datasource/JPA/Hibernate/ApplicationContext 실패를 테스트 전용 DB 설정으로 해결하는 것이었다.
  - 현재 남은 실패는 datasource 설정 단계가 아니라 `OrderServiceTest`의 동시 주문 시나리오 assertion 실패다.
  - 테스트 로그상 H2에서 `select ... for update`가 실행되고, 이후 Kafka producer 설정 로그가 출력된다. 이는 주문 서비스 동시성 검증 방식, Kafka 전송 의존성, H2와 MySQL의 락/트랜잭션 차이를 별도로 다뤄야 하는 영역이다.
- 분리 필요성: 별도 작업으로 분리해야 한다.
  - 후보 작업 1: `OrderServiceTest`에서 Kafka producer를 mock/test double로 대체해 외부 Kafka 의존을 제거한다.
  - 후보 작업 2: 포인트 동시성 검증을 H2 기반 단위/통합 테스트와 MySQL 기반 통합 테스트로 분리한다.
  - 후보 작업 3: Kafka 설정을 test profile에서 외부 브로커에 의존하지 않도록 별도 구성한다.

### 통과 여부

- 통과 여부: 부분 통과
- 통과한 항목:
  - 테스트 전용 datasource 설정 추가
  - H2 테스트 런타임 의존성 추가
  - Spring Boot 테스트의 test profile 활성화
  - `DB_URL` 미치환, Hibernate Dialect 결정, JPA `entityManagerFactory`, ApplicationContext 로딩 문제 해소
- 통과하지 못한 항목:
  - 전체 `./gradlew test` 성공
  - `OrderServiceTest`의 동시 주문 assertion 성공

### 다음 확인 사항

- 별도 Plan 단계에서 `OrderServiceTest` 실패 원인을 분리한다.
- Kafka producer 외부 의존을 테스트에서 제거할지 검토한다.
- H2로 검증할 테스트와 MySQL/Testcontainers로 검증할 동시성 테스트를 나눌지 검토한다.
