# Commit Convention

커밋 메시지는 작업 의도와 영향 범위가 드러나도록 작성한다.

## 형식

```text
type(scope): summary
```

## Type

| type | 의미 |
| --- | --- |
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `test` | 테스트 추가 또는 수정 |
| `docs` | 문서 추가 또는 수정 |
| `chore` | 빌드, 설정, 기타 작업 |

## 작성 기준

- summary는 명령형 또는 간결한 현재형으로 작성한다.
- 한 커밋에는 하나의 의도를 담는다.
- 테스트를 실행하지 못했다면 커밋 본문 또는 PR 설명에 이유를 남긴다.

## 예시

```text
docs(agent): add harness workflow checklist
feat(menu): read popular menus with bulk menu lookup
test(point): add insufficient balance case
```
