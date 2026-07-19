# Test DB Config

## 상태

- 진행 상태: 계획
- 작성일: 2026-07-19
- 대상 기능: 테스트 실행용 datasource 설정 분리

## 작업 배경

인기 메뉴 bulk 조회 개선 후 Evaluate 단계에서 `./gradlew test`를 실행했지만 Spring Boot 테스트 ApplicationContext 로딩이 실패했다.
테스트 리포트에 따르면 JPA `entityManagerFactory` 생성 중 datasource URL이 실제 JDBC URL로 해석되지 않아 Hibernate가 Dialect를 결정하지 못했다.

이 문제는 인기 메뉴 구현 로직 자체보다 테스트 실행 환경과 datasource 설정에 가까운 문제다.
따라서 운영용 `application.yml`과 테스트용 DB 설정을 분리해, 로컬 환경 변수 또는 실제 MySQL 실행 여부와 무관하게 기본 테스트가 실행되도록 만드는 계획을 세운다.

## 현재 문제

- `Ch6ProjectApplicationTests`와 `OrderServiceTest`는 모두 `@SpringBootTest`를 사용한다.
- 테스트 클래스에 `@ActiveProfiles("test")` 또는 별도 테스트 datasource 설정이 없다.
- `src/test/resources` 디렉터리가 없으며, `src/test/resources/application.yml` 또는 `src/test/resources/application-test.yml`도 존재하지 않는다.
- `build.gradle`에는 JPA 테스트 의존성은 있지만 H2 같은 테스트용 인메모리 DB 의존성은 없다.
- 이전 테스트 실패 로그에는 `Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}`가 기록되어 있다.
- 현재 `src/main/resources/application.yml`은 작업 전 변경으로 보이는 상태에서 `${DB_URL:jdbc:mysql://localhost:3306/ch6_project}`처럼 기본값이 있는 placeholder로 수정되어 있다.
- 기본값이 있더라도 테스트가 실제 MySQL에 의존하면 로컬 MySQL 실행 여부, 계정, 스키마 존재 여부에 따라 테스트가 계속 불안정할 수 있다.

## 읽은 문서와 코드

- 문서:
  - `AGENTS.md`
  - `docs/workflow/plan-guide.md`
  - `docs/logs-guide.md`
  - `docs/db/README.md`
  - `docs/logs/menu/popular/001-bulk-read.md`
- 설정:
  - `src/main/resources/application.yml`
  - `build.gradle`
  - `src/test/resources` 존재 여부
- 테스트 코드:
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`

## 확인된 현재 상태

- `docs/logs-guide.md`는 존재한다.
- `AGENTS.md`의 컨텍스트 맵에는 `docs/logs_guide.md`로 적혀 있어 실제 파일명과 차이가 있다.
- `src/test/resources`가 없으므로 테스트 전용 Spring 설정 파일도 없다.
- `Ch6ProjectApplicationTests`는 빈 `contextLoads()` 테스트이며 전체 Spring Context를 로딩한다.
- `OrderServiceTest`는 `@SpringBootTest`로 실제 Repository와 Service Bean을 주입받아 주문 동시성 시나리오를 검증한다.
- `OrderServiceTest`는 JPA Repository 저장/조회와 트랜잭션 동작에 의존한다.
- 현재 실패 원인은 테스트 코드의 assertion 실패가 아니라 ApplicationContext 생성 전 datasource/JPA 설정 단계에서 발생한다.

## 변경 대상

- 변경 예상 파일:
  - `build.gradle`
  - `src/test/resources/application-test.yml`
  - `src/test/java/com/example/ch6project/Ch6ProjectApplicationTests.java`
  - `src/test/java/com/example/ch6project/domain/order/service/OrderServiceTest.java`
- 변경하지 않을 파일:
  - `src/main/resources/application.yml`
  - 도메인 Service/Repository/Entity 구현
  - API 응답 DTO

## 접근 방향

1. 테스트가 운영 datasource 환경 변수나 실제 MySQL 실행 여부에 의존하지 않도록 테스트 profile을 분리한다.
2. `build.gradle`에 테스트 런타임용 인메모리 DB 의존성 추가를 검토한다.
   - 우선 후보: `testRuntimeOnly 'com.h2database:h2'`
   - 이유: context load와 Repository 기반 테스트를 빠르게 실행할 수 있고 별도 MySQL 서버가 필요 없다.
3. `src/test/resources/application-test.yml`을 생성해 테스트 전용 datasource를 명시한다.
   - 예: `jdbc:h2:mem:ch6_project_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
   - driver: `org.h2.Driver`
   - JPA ddl-auto: `create-drop`
4. 테스트에서 test profile을 활성화하는 방법을 선택한다.
   - 선택지 A: 각 `@SpringBootTest` 클래스에 `@ActiveProfiles("test")` 추가
   - 선택지 B: `build.gradle`의 `test` task에 `systemProperty 'spring.profiles.active', 'test'` 추가
5. 우선은 명시성이 높은 선택지 A를 검토한다.
   - 이유: 어떤 테스트가 test 설정을 사용하는지 테스트 코드에서 바로 드러난다.
   - 단점: Spring Boot 테스트 클래스가 늘어나면 annotation 반복이 생긴다.
6. Redis/Kafka는 이번 실패의 직접 원인이 아니므로, 실제로 context load에서 Redis/Kafka 연결을 강제하지 않는지 확인한 뒤 필요한 최소 설정만 테스트 profile에 둔다.
7. `OrderServiceTest`의 동시성 검증은 H2와 MySQL의 락/격리 동작 차이가 있을 수 있으므로, 단기 목표는 ApplicationContext와 기본 Repository 테스트 안정화로 둔다.
8. 동시성 정확성 검증은 별도 단계에서 MySQL 기반 통합 테스트 또는 Testcontainers 도입 여부를 검토한다.

## 통과 기준

- `./gradlew test` 실행 시 datasource URL이 `${DB_URL}` 문자열 그대로 전달되지 않는다.
- `Ch6ProjectApplicationTests.contextLoads()`가 ApplicationContext를 정상 로딩한다.
- `OrderServiceTest`가 datasource/JPA 설정 문제로 실패하지 않는다.
- 테스트용 설정은 `src/test/resources` 아래에 분리되어 운영 설정을 오염시키지 않는다.
- 운영 `src/main/resources/application.yml`의 Redis key, Kafka 설정, API 설정은 변경하지 않는다.
- 테스트나 컴파일을 실행하지 못한 경우 성공으로 표시하지 않고 이유를 기록한다.

## 테스트/검증 계획

- `./gradlew test`를 실행한다.
- 실패 시 테스트 리포트에서 실패 원인을 확인한다.
  - `build/test-results/test/TEST-com.example.ch6project.Ch6ProjectApplicationTests.xml`
  - `build/test-results/test/TEST-com.example.ch6project.domain.order.service.OrderServiceTest.xml`
- 필요하면 `./gradlew compileJava` 또는 `./gradlew compileTestJava`로 컴파일 범위를 나누어 확인한다.
- 샌드박스에서 `.gradle` lock 파일 또는 Gradle file lock 소켓 권한 문제가 반복되면 그 사실을 기록하고, 승인된 외부 실행 결과와 구분한다.
- 테스트 성공 시 `docs/logs/`에 실행 명령과 결과를 남긴다.

## 남은 리스크

- H2 MySQL mode가 실제 MySQL의 모든 SQL 문법, 락, 트랜잭션 동작을 완전히 재현하지는 않는다.
- `OrderServiceTest`의 목적이 포인트 중복 차감 방지라면 H2 통과만으로 운영 MySQL 동시성 안정성을 보장할 수 없다.
- Redis/Kafka 관련 Bean이 테스트 ApplicationContext에서 외부 서버 연결을 요구하면 DB 설정을 고친 뒤에도 다른 인프라 의존 실패가 드러날 수 있다.
- `application.yml`에 이미 기본 datasource 값이 추가된 상태인데, 이 변경의 의도와 소유자가 불명확하므로 Generate 단계에서 운영 설정을 추가로 수정할지는 신중히 판단해야 한다.
- `AGENTS.md`의 로그 문서 경로와 실제 `docs/logs-guide.md` 파일명이 다르므로, 향후 문서 정합성 정리가 필요할 수 있다.
