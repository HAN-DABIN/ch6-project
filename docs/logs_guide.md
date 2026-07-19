# Logs Guide

개발 로그는 Agent가 수행한 작업의 과정과 결과를 남기는 기록이다.
로그는 다음 작업자가 같은 실수를 반복하지 않도록 돕고, 구현 결과의 근거를 제공한다.

## 목적

- 작업 요청과 의도를 남긴다.
- 어떤 파일을 읽고 수정했는지 기록한다.
- 실패한 시도와 해결 과정을 남긴다.
- 테스트 또는 API 확인 결과를 증거로 남긴다.

## 위치

로그는 `docs/logs/` 아래에 작성한다.

```text
docs/logs/{도메인}/{기능}/001-{작업명}.md
```

예시:

```text
docs/logs/menu/popular/001-bulk-read.md
docs/logs/order/payment/001-create-order.md
docs/logs/point/charge/001-charge-point.md
```

## 작성 원칙

- 성공한 작업뿐 아니라 실패한 시도도 기록한다.
- 테스트를 실행하지 못했다면 실행하지 못한 이유를 쓴다.
- 실제로 읽거나 수정한 파일만 기록한다.
- 추측한 내용을 사실처럼 쓰지 않는다.
- 원시 로그 전체를 붙이지 말고 핵심 메시지만 요약한다.

## 로그 템플릿

```md
# 001-{작업명}

## 요청

- 사용자 요청:
- 작업 의도:

## Attempt 1 - YYYY-MM-DD

### 시도

- 접근 방식:
- 읽은 파일:
- 수정한 파일:

### 결과

- 실행한 검증:
- 결과:
- 실패 원인:

### 증거

- 테스트 명령:
- API 요청:
- API 응답 요약:

### 결정 사항

- 결정:
- 이유:

### 남은 리스크

- 리스크:
- 다음 확인 사항:
```

## 예시

```md
# 001-bulk-read

## 요청

- 사용자 요청: 인기 메뉴 조회에서 개별 findById 반복 조회를 bulk 조회로 개선한다.
- 작업 의도: 인기 메뉴 조회 시 DB 호출 수를 줄인다.

## Attempt 1 - 2026-07-19

### 시도

- 접근 방식: Redis ZSET에서 Top 3 menuId를 구한 뒤 `findAllById`로 메뉴 정보를 한 번에 조회한다.
- 읽은 파일:
  - `MenuRankingService.java`
  - `MenuRepository.java`
- 수정한 파일:
  - `MenuRankingService.java`

### 결과

- 실행한 검증: `./gradlew test`
- 결과: 실행하지 못함
- 실패 원인: 로컬 Gradle 또는 외부 서비스 실행 환경 확인 필요

### 증거

- 테스트 명령: `./gradlew test`
- API 요청: 미확인
- API 응답 요약: 미확인

### 결정 사항

- 결정: 인기 메뉴 상세 정보는 개별 조회 대신 bulk 조회를 우선 사용한다.
- 이유: Top 3 조회에서도 반복 DB 호출을 줄이고, 향후 Top N 확장에 대비하기 위해서다.

### 남은 리스크

- 리스크: Redis/Kafka가 로컬에서 실행되지 않으면 통합 검증이 제한된다.
- 다음 확인 사항: Docker 기반 Redis/Kafka 실행 후 인기 메뉴 API 응답 확인
```
