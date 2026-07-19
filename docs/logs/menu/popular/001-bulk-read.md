# 001-bulk-read

## 요청

- 사용자 요청: 인기 메뉴 조회에서 stream 내부 개별 `findById` 반복 조회를 제거하고 `findAllById` bulk 조회 방식으로 개선한다.
- 작업 의도: 인기 메뉴 Top 3 응답 생성 시 메뉴 상세 조회 DB 호출을 반복 조회에서 bulk 조회 중심으로 줄인다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식: `MenuRankingService#getPopularMenus()`에서 주문 수 내림차순 상위 3개 entry를 먼저 `List`로 고정하고, 메뉴 ID 목록을 `findAllById`로 한 번에 조회한 뒤 `Map<Long, Menu>`로 재구성했다. 이후 기존 인기 메뉴 순서를 유지하면서 `PopularMenuResponse`를 생성했다.
- 읽은 파일:
  - `AGENTS.md`
  - `docs/workflow/generate-guide.md`
  - `docs/dev/ongoing/popular-menu-bulk-read.md`
  - `docs/logs_guide.md`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
  - `src/main/java/com/example/ch6project/domain/menu/repository/MenuRepository.java`
  - `src/main/java/com/example/ch6project/domain/menu/dto/PopularMenuResponse.java`
- 수정한 파일:
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
  - `docs/logs/menu/popular/001-bulk-read.md`

### 결과

- 실행한 검증: `./gradlew compileJava`, `./gradlew --no-daemon compileJava`
- 결과: 실행 실패
- 실패 원인: Gradle이 빌드를 시작하는 과정에서 파일락 처리용 소켓을 생성하지 못해 `java.net.SocketException: Operation not permitted`가 발생했다. 최초 실행에서는 `/Users/handabin/.gradle/wrapper/dists/.../gradle-9.5.1-bin.zip.lck` 접근 권한 문제도 발생했으며, `.gradle` 쓰기 권한을 받은 뒤에는 소켓 권한 문제로 실패했다.

### 증거

- 테스트 명령: 실행하지 않음
- 컴파일 명령:
  - `./gradlew compileJava`
  - `./gradlew --no-daemon compileJava`
- API 요청: 미확인
- API 응답 요약: 미확인

### 결정 사항

- 결정: 인기 메뉴 상세 정보 조회는 상위 메뉴 ID 목록을 만든 뒤 `findAllById`로 한 번에 조회한다.
- 이유: 기존 구현은 상위 3개 entry마다 `findById`를 호출해 반복 DB 조회가 발생했다. `findAllById`로 bulk 조회하면 조회 횟수를 줄이면서도, 결과를 `Map`으로 재구성해 기존 주문 수 정렬 순서를 유지할 수 있다.
- 결정: bulk 조회 후 메뉴가 누락되면 기존 `findById(...).orElseThrow(...)`와 동일하게 `CustomException(ErrorCode.MENU_NOT_FOUND)`를 던진다.
- 이유: API 응답 필드와 Redis key, Kafka Consumer를 바꾸지 않는 조건 안에서 기존 오류 처리 의미를 유지하기 위해서다.

### 남은 리스크

- 리스크: Gradle이 샌드박스의 소켓 권한 제한으로 시작되지 않아 컴파일 성공 여부를 확인하지 못했다.
- 리스크: Redis/Kafka/MySQL 실행 환경에서의 통합 동작은 확인하지 못했다.
- 다음 확인 사항: 로컬 권한 제한이 없는 터미널에서 `./gradlew compileJava`와 필요한 테스트를 실행한다.

## Evaluate 기록 - 2026-07-19

### 평가 대상

- 계획 문서: `docs/dev/ongoing/popular-menu-bulk-read.md`
- 구현 파일: `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
- 로그 파일: `docs/logs/menu/popular/001-bulk-read.md`

### 계획 대비 구현 확인

- 일치: 최근 7일 Redis ZSET 조회, 메뉴 ID별 주문 수 합산, 주문 수 내림차순 상위 3개 선택 흐름은 유지했다.
- 일치: stream 내부 개별 `findById` 반복 호출을 제거했다.
- 일치: 상위 3개 메뉴 ID 목록으로 `findAllById`를 한 번 호출한다.
- 일치: `findAllById` 결과 순서가 보장되지 않는 점을 고려해 `Map<Long, Menu>`로 재구성한 뒤, 기존 인기 메뉴 entry 순서대로 응답을 생성한다.
- 일치: API 응답 필드, Redis key, Kafka Consumer는 변경하지 않았다.
- 일치: bulk 조회 후 메뉴가 누락되면 기존과 동일하게 `CustomException(ErrorCode.MENU_NOT_FOUND)`를 던진다.

### 실행한 검증

- `./gradlew test`
  - 샌드박스 내부 실행 결과: 실패
  - 실패 원인: `/Users/handabin/.gradle/wrapper/dists/.../gradle-9.5.1-bin.zip.lck` 접근 중 `Operation not permitted`
- `./gradlew test`
  - 샌드박스 외부 승인 실행 결과: 실패
  - 실행된 작업: `compileJava`, `processResources`, `classes`, `compileTestJava`, `testClasses`, `test`
  - 확인된 점: `compileJava`와 `compileTestJava`는 통과했다.
  - 실패 테스트: `Ch6ProjectApplicationTests > contextLoads()`, `OrderServiceTest > 동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다()`
  - 실패 원인 요약: Spring ApplicationContext 로딩 중 JPA `entityManagerFactory` 생성 실패. 테스트 리포트에 따르면 `Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}`가 발생했고, Hibernate가 JDBC metadata를 얻지 못해 Dialect를 결정하지 못했다.

### 평가 결과

- 통과 여부: 부분 통과
- 통과한 항목:
  - 계획 범위 안에서 `MenuRankingService`만 구현 변경했다.
  - 반복 `findById` 호출을 제거하고 `findAllById` bulk 조회로 변경했다.
  - `findAllById` 결과를 `Map`으로 재구성해 인기 메뉴 순서를 유지했다.
  - `./gradlew test` 실행 과정에서 컴파일 단계는 통과했다.
- 통과하지 못한 항목:
  - 전체 테스트는 실패했다.
  - API 응답은 직접 호출로 검증하지 못했다.
  - Redis/Kafka/MySQL 통합 동작은 검증하지 못했다.

### 남은 리스크

- 테스트 환경에서 `${DB_URL}`이 실제 JDBC URL로 치환되지 않아 Spring Boot 통합 테스트가 실패한다.
- 인기 메뉴 전용 단위 테스트가 없어 `findAllById` 결과 순서가 뒤섞이는 상황을 자동 검증하지 못했다.
- Redis ZSET 실제 데이터 기반 `GET /api/menus/popular` 응답은 확인하지 못했다.

### 다음 확인 사항

- 테스트용 DB 설정 또는 test profile을 정리한 뒤 `./gradlew test`를 다시 실행한다.
- `MenuRankingService` 단위 테스트를 추가해 bulk 조회, 순서 유지, 메뉴 누락 예외 처리를 검증한다.
- Redis 테스트 데이터가 준비된 환경에서 `GET /api/menus/popular` 응답을 확인한다.
