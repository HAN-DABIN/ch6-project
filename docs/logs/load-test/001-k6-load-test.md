# 001-k6-load-test

## 요청

- 사용자 요청: k6 기반 메뉴 조회와 인기 메뉴 조회 부하 테스트 스크립트 및 실행 문서를 추가한다.
- 작업 의도: `GET /api/menus`와 `GET /api/menus/popular`의 현재 구현 기준 조회 전략 응답 특성을 비교할 수 있게 한다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식:
  - 승인된 `docs/dev/ongoing/k6-load-test.md` 계획을 다시 읽었다.
  - 애플리케이션 코드는 수정하지 않았다.
  - `BASE_URL` 환경 변수를 받는 k6 스크립트를 추가했다.
  - `K6_SCENARIO`로 `smoke`, `menus`, `popular`, `mixed` 시나리오를 선택할 수 있게 했다.
  - 메뉴 조회와 인기 메뉴 조회 각각에 `api` 태그를 부여해 k6 결과에서 구분할 수 있게 했다.
  - HTTP status, 공통 응답 status, 응답 메시지, `data` 배열 여부를 check로 검증하게 했다.
  - thresholds를 추가해 실패율과 p95를 확인할 수 있게 했다.
  - 주문/결제 부하 테스트는 인증, 포인트 seed, Kafka/Redis/MySQL 상태 준비가 필요하므로 실행 스크립트가 아니라 후속 방향으로 문서화했다.
- 읽은 파일:
  - `docs/workflow/generate-guide.md`
  - `docs/dev/ongoing/k6-load-test.md`
  - `src/main/java/com/example/ch6project/domain/menu/controller/MenuController.java`
  - `src/main/java/com/example/ch6project/domain/order/controller/OrderController.java`
  - `src/main/java/com/example/ch6project/domain/menu/service/MenuService.java`
  - `src/main/java/com/example/ch6project/domain/menu/ranking/MenuRankingService.java`
- 수정한 파일:
  - `k6/menu-read-test.js`
  - `docs/load-test/k6.md`
  - `docs/logs/load-test/001-k6-load-test.md`

### 결과

- 실행한 검증:
  - `k6 version`
  - `k6 inspect k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=menus k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=popular k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=smoke k6/menu-read-test.js`
  - `git diff --check`
- 결과:
  - 로컬 k6 설치 확인 성공: `k6 v2.0.0 (commit/devel, go1.26.3, darwin/arm64)`
  - 기본 `mixed` 시나리오 inspect 성공.
  - `menus`, `popular`, `smoke` 시나리오 inspect 성공.
  - `git diff --check` 통과.
  - 실제 `k6 run` 부하 실행은 수행하지 않았다. 애플리케이션 서버, MySQL, Redis, 인기 메뉴 seed 상태를 먼저 준비해야 하기 때문이다.

### 증거

- k6 설치 확인:
  - `k6 version`
  - 결과: `k6 v2.0.0`
- k6 스크립트 해석 확인:
  - `k6 inspect k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=menus k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=popular k6/menu-read-test.js`
  - `k6 inspect -e K6_SCENARIO=smoke k6/menu-read-test.js`
  - 결과: 모두 성공.
- 문법/공백 확인:
  - `git diff --check`
  - 결과: 통과.

### 결정 사항

- 결정: 스크립트 위치는 사용자 Generate 조건의 `k6/menu-read-test.js`를 따랐다.
- 이유: 계획 문서의 위치 제안은 `load-tests/k6/...`였지만, 승인 이후 사용자 조건에서 구체 경로를 지정했기 때문이다.
- 결정: 주문/결제 부하 테스트는 이번 Attempt에서 실행 스크립트로 만들지 않고 문서 방향으로 남겼다.
- 이유: `POST /api/orders`는 JWT 인증, 테스트 사용자/포인트 seed, Kafka, Redis, MySQL 준비가 필요해 조회 API 비교 스크립트와 섞으면 결과 해석이 흐려질 수 있기 때문이다.

### 남은 리스크

- 리스크: Redis 인기 메뉴 데이터가 준비되지 않으면 `GET /api/menus/popular`는 정상이어도 빈 배열 응답 중심으로 측정된다.
- 리스크: k6는 로컬에 설치되어 있으나, 애플리케이션/외부 인프라 실행 여부에 따라 실제 부하 테스트 실행 가능성이 달라진다.
- 다음 확인 사항:
  - 애플리케이션, MySQL, Redis를 실행한 뒤 smoke 시나리오를 실행해 응답 계약을 확인한다.
  - 인기 메뉴 데이터가 있는 상태와 없는 상태를 나누어 결과를 기록한다.

## Evaluate 기록 - 2026-07-19

### 검증 대상

- 계획 문서:
  - `docs/dev/ongoing/k6-load-test.md`
- 생성 파일:
  - `k6/menu-read-test.js`
  - `docs/load-test/k6.md`
  - `docs/logs/load-test/001-k6-load-test.md`

### 계획 대비 일치 여부

- 일치한 항목:
  - `GET /api/menus`와 `GET /api/menus/popular`를 비교할 수 있는 k6 스크립트를 추가했다.
  - `BASE_URL`을 환경 변수로 받을 수 있게 했다.
  - `K6_SCENARIO`로 `smoke`, `menus`, `popular`, `mixed` 시나리오를 선택할 수 있게 했다.
  - 각 API에 대해 HTTP status, 공통 응답 status, 응답 메시지, `data` 배열 여부 check를 포함했다.
  - 실패율과 p95 기준 thresholds를 포함했다.
  - 실행 방법과 결과 해석을 `docs/load-test/k6.md`에 문서화했다.
  - `POST /api/orders`는 실행 스크립트가 아니라 후속 부하 테스트 방향으로 문서화했다.
- 계획 대비 달라진 항목:
  - 계획 문서의 위치 제안은 `load-tests/k6/menu-read-comparison.js`였지만, Generate 단계 사용자 조건에 맞춰 `k6/menu-read-test.js`로 생성했다.
  - 계획 문서의 작업 로그 후보는 `docs/logs/load-test/k6/...`였지만, Generate 단계 사용자 조건에 맞춰 `docs/logs/load-test/001-k6-load-test.md`에 기록했다.

### 실행한 명령

- `k6 version`
- `curl -s -o /tmp/ch6_menus_check.out -w "%{http_code}" http://localhost:8080/api/menus`
- `git diff --check -- k6/menu-read-test.js docs/load-test/k6.md docs/logs/load-test/001-k6-load-test.md`

### 결과

- `k6 version`
  - 성공.
  - 확인된 버전: `k6 v2.0.0 (commit/devel, go1.26.3, darwin/arm64)`
- 애플리케이션 실행 여부 확인:
  - 실패.
  - `curl` 결과 HTTP code `000`, exit code `7`.
  - `localhost:8080`에서 애플리케이션이 응답하지 않는 상태로 판단했다.
- `git diff --check`
  - 통과.

### k6 run 실행 여부

- 실제 `k6 run`은 실행하지 않았다.
- 이유:
  - 사용자 조건은 애플리케이션이 실행 중이고 k6가 설치되어 있으면 k6 테스트를 실행하라는 것이었다.
  - k6는 설치되어 있었지만 `localhost:8080/api/menus`가 연결되지 않아 애플리케이션 실행 조건이 충족되지 않았다.
  - 이 상태에서 k6를 실행하면 스크립트 자체 검증이 아니라 서버 미실행으로 인한 연결 실패만 기록하게 된다.

### 통과 여부

- 문서/스크립트 생성 기준: 통과.
- k6 설치 확인: 통과.
- k6 스크립트 해석 확인: Generate Attempt 1에서 `k6 inspect`로 통과 확인.
- 실제 부하 테스트 실행 기준: 미검증.

### 남은 리스크

- 애플리케이션, MySQL, Redis가 실행된 상태에서 `k6 run` smoke 결과를 아직 확인하지 못했다.
- Redis 인기 메뉴 seed가 없으면 인기 메뉴 조회는 정상 응답하더라도 빈 배열 중심으로 측정될 수 있다.
- 로컬 환경의 p95 기준 `1000ms`는 초기 기준이며, 실제 기준선은 같은 seed와 같은 인프라 조건에서 반복 측정 후 조정해야 한다.
- 주문/결제 부하 테스트는 아직 실행 스크립트가 없고, 인증 사용자/포인트/Kafka/Redis/MySQL 준비가 필요한 후속 작업이다.

### 다음 확인 사항

- 애플리케이션과 MySQL, Redis를 실행한다.
- 필요하면 Redis에 `menu:ranking:{yyyy-MM-dd}` seed를 넣는다.
- 다음 명령으로 smoke를 먼저 실행한다.

```bash
BASE_URL=http://localhost:8080 K6_SCENARIO=smoke k6 run k6/menu-read-test.js
```

- smoke 통과 후 `menus`, `popular`, `mixed` 시나리오를 각각 실행해 결과를 비교한다.

## Evaluate 재실행 기록 - 2026-07-19

### 배경

- 이전 Evaluate에서는 샌드박스 내부 `curl`이 `Operation not permitted`로 실패해 애플리케이션 실행 여부를 확인하지 못했고, 실제 `k6 run`을 수행하지 않았다.
- 이후 샌드박스 외부 승인 실행으로 `http://127.0.0.1:8080/api/menus` 접근을 확인했다.

### 실행한 명령

- `curl -s -o /tmp/ch6_menus_check.out -w "%{http_code}" http://127.0.0.1:8080/api/menus`
- `k6 run -e BASE_URL=http://127.0.0.1:8080 -e K6_SCENARIO=smoke k6/menu-read-test.js`
- `curl -i --max-time 10 http://127.0.0.1:8080/api/menus/popular`
- `curl -s http://127.0.0.1:8080/api/menus`

### 결과

- 애플리케이션 실행 여부:
  - 샌드박스 외부 승인 실행 기준 `GET /api/menus`가 HTTP `200`으로 응답했다.
- k6 smoke 실행:
  - 실행됨.
  - exit code: `99`
  - 결과: 실패.
- k6 요약:
  - `checks`: `50.00%`, 120 succeeded / 120 failed
  - `http_req_failed`: `50.00%`, 30 out of 60
  - `http_req_failed{api:menus}`: `0.00%`, 0 out of 30
  - `http_req_failed{api:popular-menus}`: `100.00%`, 30 out of 30
  - `http_req_duration{api:menus}` p95: `7.15ms`
  - `http_req_duration{api:popular-menus}` p95: `11.22ms`
  - 실패한 thresholds:
    - `checks rate>0.99`
    - `http_req_failed rate<0.01`
    - `http_req_failed{api:popular-menus} rate<0.01`

### 실패 원인

- `GET /api/menus/popular` 직접 호출 결과:

```json
{
  "code": "MENU_NOT_FOUND",
  "message": "메뉴를 찾을 수 없습니다.",
  "status": 404
}
```

- `GET /api/menus` 직접 호출 결과:

```json
{
  "data": [],
  "message": "메뉴 조회 성공",
  "status": 200
}
```

- 판단:
  - 메뉴 조회 API 자체는 정상 응답한다.
  - 인기 메뉴 API는 Redis 랭킹에 있는 메뉴 ID를 MySQL `menus`에서 찾지 못해 `MENU_NOT_FOUND`로 실패하는 상태다.
  - 현재 로컬 데이터는 MySQL 메뉴 데이터가 비어 있는데 Redis 랭킹 데이터는 남아 있거나, Redis 랭킹과 MySQL 메뉴 seed가 서로 맞지 않는 것으로 판단된다.

### 통과 여부

- `k6 run` 실제 실행 기준: 실패.
- 메뉴 조회 API smoke 기준: 통과.
- 인기 메뉴 조회 API smoke 기준: 실패.
- 스크립트/문서 생성 기준: 이전 검증대로 통과.

### 남은 리스크

- Redis ZSET 랭킹 데이터와 MySQL `menus` 데이터가 불일치하면 인기 메뉴 조회가 `MENU_NOT_FOUND`로 실패한다.
- 인기 메뉴 부하 테스트는 Redis 랭킹 seed와 MySQL 메뉴 seed를 같은 메뉴 ID 기준으로 맞춘 뒤 다시 실행해야 한다.
- 현재 smoke 실패는 k6 스크립트 문법 문제가 아니라 테스트 대상 데이터 상태 문제로 보인다.

### 다음 확인 사항

- MySQL `menus`에 Redis ZSET이 참조하는 메뉴 ID가 존재하도록 seed를 맞춘다.
- 또는 Redis `menu:ranking:{yyyy-MM-dd}` key를 현재 MySQL 메뉴 ID에 맞게 비운 뒤 다시 넣는다.
- seed 정합성을 맞춘 뒤 아래 명령을 재실행한다.

```bash
k6 run -e BASE_URL=http://127.0.0.1:8080 -e K6_SCENARIO=smoke k6/menu-read-test.js
```

## Seed 및 smoke 재실행 기록 - 2026-07-19

### 배경

- 이전 k6 smoke는 `GET /api/menus/popular`가 `MENU_NOT_FOUND`를 반환해 실패했다.
- 원인은 Redis 랭킹이 참조하는 메뉴 ID와 MySQL `menus` 데이터가 일치하지 않는 상태로 판단했다.
- 사용자 요청에 따라 메뉴 더미데이터와 Redis 인기순위를 같은 메뉴 ID 기준으로 주입했다.

### 주입한 데이터

- MySQL `menus`:
  - `1`, `아메리카노`, `4500`, `ACTIVE`
  - `2`, `카페라떼`, `5000`, `ACTIVE`
  - `3`, `바닐라라떼`, `5500`, `ACTIVE`
- Redis ZSET:
  - key: `menu:ranking:2026-07-19`
  - member `1`, score `30`
  - member `2`, score `20`
  - member `3`, score `10`
- 정리한 Redis key:
  - `menu:ranking:2026-07-19`
  - `menu:ranking:2026-07-18`
  - `menu:ranking:2026-07-17`
  - `menu:ranking:2026-07-16`
  - `menu:ranking:2026-07-15`
  - `menu:ranking:2026-07-14`
  - `menu:ranking:2026-07-13`

### API 확인

- `GET /api/menus`:
  - HTTP `200`
  - 메뉴 3건 반환.
- `GET /api/menus/popular`:
  - HTTP `200`
  - 인기 메뉴 3건 반환.
  - 응답 순서: 아메리카노 30, 카페라떼 20, 바닐라라떼 10.

### k6 smoke 재실행

- 실행한 명령:

```bash
k6 run -e BASE_URL=http://127.0.0.1:8080 -e K6_SCENARIO=smoke k6/menu-read-test.js
```

- 결과:
  - exit code: `0`
  - `checks`: `100.00%`, 240 out of 240
  - `http_req_failed`: `0.00%`, 0 out of 60
  - `http_req_failed{api:menus}`: `0.00%`, 0 out of 30
  - `http_req_failed{api:popular-menus}`: `0.00%`, 0 out of 30
  - `http_req_duration{api:menus}` p95: `6.5ms`
  - `http_req_duration{api:popular-menus}` p95: `6.92ms`
  - 모든 thresholds 통과.

### 통과 여부

- 실제 k6 smoke 기준: 통과.
- 메뉴 조회 API smoke 기준: 통과.
- 인기 메뉴 조회 API smoke 기준: 통과.

### 남은 리스크

- 이번 seed는 로컬 수동 더미데이터이므로 재실행 가능한 자동 seed 절차는 아직 없다.
- Redis 랭킹과 MySQL 메뉴 데이터가 다시 불일치하면 인기 메뉴 API는 `MENU_NOT_FOUND`로 실패할 수 있다.
- smoke는 낮은 부하의 응답 계약 확인이며, `menus`, `popular`, `mixed` 부하 시나리오의 기준선 측정은 별도로 실행해야 한다.
