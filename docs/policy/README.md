# Policy README

이 폴더는 커피숍 주문 시스템의 도메인 정책을 기록한다.
Agent는 기능을 구현하기 전에 관련 정책 문서를 먼저 확인해야 한다.

## 정책 목록

| 문서 | 설명 |
| --- | --- |
| `point-policy.md` | 포인트 충전, 사용, 이력, 동시성 정책 |
| `order-payment-policy.md` | 주문, 결제, 트랜잭션, 결제 완료 이벤트 정책 |
| `popular-menu-policy.md` | 인기 메뉴 집계, Redis 랭킹, 조회 최적화 정책 |