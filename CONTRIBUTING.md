# Contributing

## 브랜치 & PR

- `feat/PK-<티켓번호>-<설명>` 브랜치에서 작업 → `develop`으로 PR
- 릴리즈: `develop` → `main` (버전업 커밋: `chore: x.y.z로 버전업`)
- PR 제목에 JIRA 티켓 번호를 포함하고 [PR 템플릿](.github/pull_request_template.md)을 따른다.
- PR 전에 `./gradlew build`가 통과하는지 확인한다.

## 커밋 메시지

한국어 + Conventional Commits 접두사 (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `chore(deps):`)

```
feat: 핑퐁 답변 작성 기능 추가
```

## 코드 규칙

1. Value Object / 도메인 모델로의 매핑은 **application** 계층에서 한다. (Primitive → Value Object, 입력 DTO → 도메인 모델)
2. DB 스키마 변경은 Flyway 마이그레이션(`src/main/resources/db/migration/V{n}__*.sql`)으로만 한다.
3. API 문서의 에러 상태 코드마다 **발생 상황, 에러 코드, UI 메시지**(`- UI 메시지: "..."`)를 적는다. (예시: `PingPongRoute.kt`의 `createPingPongAnswerDocs()`)
4. 운영 서버 사양이 작으므로 쿼리 수를 최소화하고, 인덱스와 페이지네이션을 고려한다.

## 테스트 규칙

- **JUnit 4만** 사용한다. JUnit 5 어노테이션은 컴파일되지만 실행되지 않는다.
- 테스트 이름은 한국어 백틱 문장으로 쓰고, `// given` · `// when` · `// then` 구조를 따른다.
- 엔드포인트 테스트는 `TestEndpoint` 도구를 사용한다.
- 새 테이블은 `TestDatabaseFactory.kt`와 `PostgresRule.kt` 양쪽에 등록한다.

자세한 테스트 컨벤션은 [CLAUDE.md](CLAUDE.md#testing)를 참고한다.
