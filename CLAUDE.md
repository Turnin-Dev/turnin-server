# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Kotlin 2.1.10 + Ktor 3.1.2 (Netty engine), Gradle Kotlin DSL, JDK 17. Exposed 0.61.0 (ORM) over PostgreSQL, Koin 4.0.3 + KSP for DI, Flyway for migrations, MockK + Testcontainers (Postgres) for tests.

## Architecture

Single Gradle module. Package-based modular monolith with Clean Architecture per feature (`presentation → application → domain ← infrastructure`). Top-level packages: `common/` (cross-cutting: di, jwt, db, batch, exception, firebase, ml, plugin, route, util) and `domain/<feature>/` (account, announcement, auth, block, discover, feed, file, friend, keyword, notification, report, user, userKeyword).

Layering conventions and dependency-direction rules are documented in @README.md, and code/commit rules in @CONTRIBUTING.md (draft — may change;) — read them before adding a new feature or layer.

`domain/seed/` is a one-off seed-data insertion module — never wire it into normal application code, it exists only for initial data seeding (see its own README).

## Commands

- Build: `./gradlew build` (includes ktlintCheck + tests)
- Run locally: `./gradlew runDev` (loads `.env.dev` + `application-dev.conf`) or `./gradlew runProd` (prod equivalent)
- Lint: `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
- Fat jar for deploy: `./gradlew buildFatJar` → `turnin-api.jar`
- Tests: `./gradlew test` — this also triggers `koinTest` (finalizedBy), a separate task that runs `HardDeleteExpiredAccountsUseCaseIntegrationTest` and `AccountDeletionIntegrationTest` in one Gradle test process (`maxParallelForks = 1`) because they share Koin DI container state. The regular `test` task allows up to `Runtime.getRuntime().availableProcessors()` Gradle test processes for the other tests. Don't assume `koinTest` ran just because `test` passed — check both task results.
- Integration tests use Testcontainers (Postgres) — **Docker must be running locally** or these tests fail immediately.
- Single test class: `./gradlew test --tests "com.turnin.<package>.<TestClassName>" -x koinTest`, then confirm it actually ran in `build/test-results/test/TEST-<fully.qualified.Name>.xml`.
- Sandboxed sessions: Gradle builds must run on a daemon started **outside** the Claude Code sandbox. A daemon spawned inside the sandbox cannot read `application-prod.conf` (read-denied secret), so `processResources` fails with `Operation not permitted`. If you see that error, or the build reuses a sandboxed daemon, ask the user to run `! ./gradlew --stop && ./gradlew help`, then retry. Do not loosen the secret read-deny and do not fall back to IntelliJ MCP run tools.

## Skills

Project skills live in `.claude/skills/`. Use them instead of improvising the same steps.

| Skill | Use when |
|---|---|
| `write-tests` | Writing or planning tests for any code in this repo. Lists Given-When-Then test cases first, writes them, then compiles, runs, and lints them. Pass `cases-only` to stop at the list. |
| `verify` | After code changes, before saying the work is done: `ktlintCheck` + full `test` + `koinTest`. |
| `run-dev` | Starting the server locally. |

## Testing

When asked to write tests, invoke the `write-tests` skill; its `rules/` hold the full conventions. The points below apply to every test in this repo, even without the skill.

- **JUnit 4 only.** Use `kotlin.test.Test` / `org.junit.Test`, `org.junit.Before` / `After` / `Rule` / `Ignore`. JUnit 5 annotations (`@BeforeEach`, `@Nested`, `@ParameterizedTest`, `@Disabled`, ...) compile but are silently ignored by the runner.
- **Names:** `{TestedClass}Test` in the same package path under `src/test/kotlin`; test functions are Korean backtick sentences stating condition and outcome (`` `신고 대상이 전부 없는 경우 도메인 예외가 발생한다` ``). Route tests prefix the endpoint (`` `신고 생성 - 토큰 없이 요청 시 401 에러를 반환한다` ``).
- **Structure:** `// given`, `// when`, `// then`; one scenario per test; values the assertion depends on are visible inside the test; no logic or computed expected values in assertions.
- **Libraries:** `kotlin.test` assertions, MockK (`coEvery` / `coVerify`, `slot` to check objects passed to dependencies), `runTest` for suspend code. Mock repository/provider interfaces only — never the class under test, DTOs, value objects, or domain models.
- **By layer:**
  - Use case → real use case with mocked dependencies.
  - Repository impl → real SQL on `TestDatabaseFactory` (H2, default) or `PostgresRule` (Testcontainers; only for PostgreSQL-specific queries such as pgvector).
  - Route → `testApplication` + `TestEndpoint` tools (`testGetEndpoint`, `testPostEndpoint`, ...) with `testPlugin(authRouting = { ... })` and a mocked `...UseCases`.
- **What to cover:** every code branch, each exception type, and each concrete HTTP status as its own test (never merge 400/401/403); both sides of each validation boundary. Skip same-class input variations, collection-size variations, and null for non-nullable parameters.
- **New tables** must be registered in both `src/test/kotlin/util/db/TestDatabaseFactory.kt` and `PostgresRule.kt` (enum map and every `SchemaUtils.create` / `drop` list), or they do not exist in tests.
- **Never change production code to make a test pass.**
- **Never change an expected value to match the implementation.** Expected values come from the contract (`docs/spec/`, KDoc, API docs), not from what the code currently returns. If a test fails because the implementation differs from the contract, keep the expectation and report the implementation defect.

## Conventions

- Branch flow: feature branches → PR into `develop`; `develop` → `main` for releases (version-bump commits like `chore: 1.5.0로 버전업`).
- Commit messages: Korean, Conventional-Commits-style prefixes (`feat:`, `fix:`, `refactor:`, `chore:`, `test:`, `chore(deps):`).
- API docs (the `RouteConfig.*Docs()` functions at the bottom of each route file): every error status code description states the case, the error code when there is one, and the message the app UI should show for it (`- UI 메시지: "..."`). When one status covers several cases with different messages, list each case separately. See `createPingPongAnswerDocs()` in `PingPongRoute.kt` for the format.
- Endpoint tests use a custom `TestEndpoint` test tool (see existing tests under `src/test/kotlin` for the pattern) rather than raw Ktor test client calls.

## Secrets

- `.env.dev`, `.env.localtest`, `application-*.conf`, and `firebase-service-account.json` contain real credentials/config. Never print, echo, or commit their contents — reference key names only.
- Keep `.env.prod` separately


## Leave alone

These are generated or local-only and shouldn't be committed or regenerated unless explicitly asked: `docs/` (Swagger-Codegen-generated API docs), `seed/` (seed data files), `logs/`, `hs_err_pid*.log`, `opentelemetry-javaagent.jar` (optional local javaagent override).
