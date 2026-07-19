# Popular Menu Bulk Read

## 상태

- 진행 상태: 계획
- 작성일: 2026-07-19
- 대상 기능: 최근 7일 인기 메뉴 Top 3 조회

## 요청 해석

- 사용자 요청: 인기 메뉴 조회에서 메뉴 상세 정보를 개별 `findById` 반복 조회하지 않고 `findAllById` bulk 조회로 개선한다.
- 기능 단위:
  - Redis ZSET에서 최근 7일 메뉴별 주문 수를 합산한다.
  - 주문 수 기준 상위 3개 메뉴 ID를 선택한다.
  - 선택된 메뉴 ID 목록으로 메뉴 정보를 한 번에 조회한다.
  - 조회된 메뉴 정보와 주문 수를 조합해 인기 메뉴 응답을 만든다.

## 읽은 문서와 코드

- 문서:
  - `AGENT.md`
  - `docs/workflow/plan-guide.md`
  - `docs/policy/popular-menu-policy.md`
  - `docs/api/README.md`
  - `docs/db/README.md`
  - `README.md`
  - `docs/ongoing/popular-menu-bulk-read.md`
- 코드:
  - `src/main/java` 전체 파일 목록
  - `build.gradle`
  - `src/main/java/com/example/ch6project/Ch6ProjectApplication.java`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
  - `src/main/java/com/example/ch6project/domain/menu/repository/MenuRepository.java`
  - `src/main/java/com/example/ch6project/domain/menu/dto/PopularMenuResponse.java`
  - `src/main/java/com/example/ch6project/domain/menu/controller/MenuController.java`
  - `src/main/java/com/example/ch6project/domain/menu/service/MenuService.java`
  - `src/main/java/com/example/ch6project/domain/menu/entity/Menu.java`

## 확인된 현재 상태

- 프로젝트에는 `AGENTS.md`가 없고 `AGENT.md`가 존재한다.
- `AGENT.md`는 진행 중인 작업 문서 위치를 `docs/ongoing/`으로 안내한다.
- 이번 사용자 요청은 `docs/dev/ongoing/popular-menu-bulk-read.md` 생성을 명시했으므로 사용자 요청을 우선해 이 문서를 작성한다.
- `src/main/java`에는 auth, menu, order, payment, point, pointHistory, common 패키지 구현이 존재한다.
- `MenuRankingService#getPopularMenus()`는 최근 7일 Redis ZSET key를 순회하며 메뉴 ID별 주문 수를 `Map<Long, Long>`에 합산한다.
- `MenuRankingService#getPopularMenus()`는 합산 결과를 주문 수 내림차순으로 정렬하고 상위 3개로 제한한다.
- 현재 개선 대상은 상위 3개 entry를 `stream().map(...)`으로 처리하면서 `menuRepository.findById(entry.getKey())`를 반복 호출하는 부분이다.
- `MenuRepository`는 `JpaRepository<Menu, Long>`를 상속하므로 별도 메서드 추가 없이 기본 `findAllById(Iterable<Long>)`를 사용할 수 있다.
- `PopularMenuResponse`는 `menuId`, `name`, `price`, `orderCount`를 응답하며 `PopularMenuResponse.from(Menu menu, Long orderCount)` 팩터리 메서드를 제공한다.
- `MenuController#getPopularMenus()`는 `GET /api/menus/popular` 요청을 받아 `MenuRankingService#getPopularMenus()` 결과를 `CommonApiResponse`로 감싼다.
- `MenuService#getMenus()`는 일반 메뉴 목록 조회에서 `menuRepository.findAll()`을 사용하므로 이번 변경의 직접 대상은 아니다.
- `Menu` Entity에는 `id`, `name`, `price`, `status` 필드와 `isActive()` 메서드가 있다.
- `build.gradle`에는 JPA, Redis, Kafka, WebMVC, Security, Validation 의존성이 포함되어 있다.

## 영향 범위

- 변경 예상 파일:
  - `src/main/java/com/example/ch6project/**/menu/**/MenuRankingService.java`
  - `src/main/java/com/example/ch6project/**/menu/**/MenuRepository.java`
  - `src/main/java/com/example/ch6project/**/menu/**/PopularMenuResponse.java`
  - `src/main/java/com/example/ch6project/**/menu/**/MenuController.java`
  - 관련 테스트 파일
- 영향받는 API:
  - `GET /api/menus/popular`
- 영향받는 테이블/외부 시스템:
  - `menus`
  - Redis ZSET 인기 메뉴 랭킹 key
  - Kafka `PaymentCompletedEvent` 기반 랭킹 갱신 흐름

## 접근 방향

1. Generate 단계 시작 시 실제 메뉴/랭킹 관련 패키지와 클래스가 존재하는지 다시 확인한다.
2. Redis ZSET에서 오늘 포함 최근 7일 key를 조회하고, 메뉴 ID별 score를 합산하는 기존 로직을 유지한다.
3. 합산 결과를 주문 횟수 내림차순으로 정렬하고 상위 3개 메뉴 ID만 선택한다.
4. 상위 메뉴 ID 목록을 `MenuRepository.findAllById(menuIds)`로 한 번에 조회한다.
5. `findAllById` 결과는 입력 ID 순서를 보장하지 않을 수 있으므로 `Map<Long, Menu>` 형태로 재구성한다.
6. 정렬된 상위 메뉴 ID 순서를 기준으로 메뉴 정보와 주문 횟수를 조합해 `PopularMenuResponse`를 생성한다.
7. 조회된 메뉴가 없거나 삭제/비활성 등으로 누락된 메뉴가 있으면 정책에 맞게 제외하거나 별도 오류 처리 기준을 정한다.
8. API 응답 필드가 `docs/api/README.md`와 `README.md` 사이에서 일부 다르므로, Generate 단계에서 실제 DTO와 우선 문서 기준을 다시 확인한다.

## 통과 기준

- 인기 메뉴 데이터가 없으면 빈 배열을 반환한다.
- 최근 7일 Redis ZSET score가 메뉴 ID 기준으로 합산된다.
- 주문 횟수 내림차순으로 상위 3개만 반환된다.
- 메뉴 상세 조회는 상위 메뉴별 개별 `findById` 반복이 아니라 `findAllById` 1회 호출 중심으로 처리된다.
- `findAllById` 결과 재정렬 후에도 인기 메뉴 순서가 주문 수 순서와 일치한다.
- 기존 메뉴 목록 조회 API에는 영향을 주지 않는다.
- 테스트를 실행하지 못한 경우 그 이유를 Evaluate 기록에 남긴다.

## 테스트/검증 기준

- 단위 테스트:
  - Redis 랭킹 데이터가 비어 있으면 빈 응답을 반환한다.
  - 여러 날짜에 흩어진 같은 메뉴 ID의 주문 수를 합산한다.
  - 상위 3개만 선택한다.
  - `findAllById` 결과 순서가 뒤섞여도 응답 순서는 주문 수 순서를 유지한다.
  - 메뉴 정보가 일부 누락된 경우의 처리 방식을 검증한다.
- API 검증:
  - `GET /api/menus/popular`가 `CommonApiResponse` 형식으로 응답한다.
  - 응답 필드가 최종 API 문서와 일치한다.
- 명령어:
  - 가능한 경우 `./gradlew test`
  - 필요 시 `./gradlew compileJava`

## 남은 질문 또는 리스크

- `findAllById`는 결과 순서를 보장하지 않으므로, 상위 3개 ID의 정렬 순서를 유지하려면 조회 결과를 `Map<Long, Menu>`로 재구성해야 한다.
- 현재 `findById` 반복 조회는 메뉴가 없으면 `CustomException(ErrorCode.MENU_NOT_FOUND)`를 던진다. bulk 조회로 바꿀 때도 메뉴 누락 시 동일하게 예외를 던질지, 누락 메뉴를 제외할지 결정해야 한다.
- `docs/api/README.md`의 인기 메뉴 응답에는 `price`가 포함되어 있지만 `README.md` 예시에는 `price`가 없다. 실제 `PopularMenuResponse`는 `price`를 포함하므로 문서 정합성 정리가 필요할 수 있다.
- 동점 메뉴 정렬 기준은 `docs/policy/popular-menu-policy.md`에서 별도 정책으로 남아 있다. 현재 코드는 주문 수 내림차순만 적용하므로 동점 상황의 응답 순서는 안정적으로 보장되지 않을 수 있다.
- Redis key prefix는 코드에서 `menu:ranking:`으로 확인되었지만, 날짜 기준은 `LocalDate.now()`를 직접 사용한다. 서버 timezone 기준이 서비스 정책과 맞는지 확인이 필요하다.
- Redis/Kafka/MySQL 실행 환경이 없으면 통합 검증이 제한될 수 있다.
