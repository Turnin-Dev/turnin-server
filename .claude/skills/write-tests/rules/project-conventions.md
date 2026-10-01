# Project Conventions (turnin-server)

Existing tests win. When a neighboring test does something differently from this file, match the neighbor.

## Framework

- Tests run on **JUnit 4** (`kotlin-test-junit`; the build does not call `useJUnitPlatform()`).
  - Use `@Test` from `kotlin.test.Test` or `org.junit.Test`, and `org.junit.Before` / `org.junit.After` / `org.junit.Rule`.
  - **Never use JUnit 5 annotations**: `@BeforeEach`, `@AfterEach`, `@Nested`, `@ParameterizedTest`, `@DisplayName`, `@Disabled`. They compile (Jupiter is on the classpath transitively) but JUnit 4 ignores them, so setup silently does not run or tests silently do not execute.
  - To skip a test use `org.junit.Ignore`.
- Assertions: `kotlin.test` (`assertEquals`, `assertTrue`, `assertNotNull`, `assertNull`, ...). `assertEquals(expected, actual)` - expected first.
- Exceptions: `org.junit.jupiter.api.assertThrows<T> { }` is the common style here (it is a plain function and works under JUnit 4); `kotlin.test.assertFailsWith<T>` is also used. Match the file. Assert the specific exception type, not just "something was thrown".
- Mocking: **MockK**. `mockk<T>()`, `coEvery` / `coVerify` for suspend functions, `every` / `verify` otherwise, `just Runs` for `Unit`. Avoid `relaxed = true`; an unstubbed call should fail loudly.
- Coroutines: `= runTest { }` for suspend code, `= testApplication { }` for route tests.

## Naming

- Class: `{TestedClass}Test`, in the same package path as the target under `src/test/kotlin`.
- Test functions: **Korean sentence in backticks** that states the condition and the outcome.
  - `` `신고 대상이 전부 없는 경우 도메인 예외가 발생한다` ``
  - `` `알림 전송 실패 시에도 상태 수정은 성공으로 처리한다` ``
  - Route tests prefix the endpoint: `` `신고 생성 - 토큰 없이 요청 시 401 에러를 반환한다` ``
- Be specific about the outcome ("401 에러를 반환한다", "null을 반환한다"), not "실패한다". Avoid a bare "성공 테스트" when the class has more than one success path; say what succeeds.
- Variables: `expected...` for expected values; the outcome as `result` (or `actual...`), matching the file.

## Structure of a test class

```kotlin
class CreateReportUseCaseTest {
    private val reportRepository = mockk<ReportRepository>()
    private val usecase = CreateReportUseCase(reportRepository)

    @Test
    fun `...`() = runTest { ... }

    companion object {
        private val TestReporterId = UserId(1L)
        private val TestReportDetailDto = ReportDetailDto(...)
    }
}
```

- Subject and mocks are `private val` properties.
- Shared test data lives in a `companion object` as `Test...` vals, derived per test with `.copy(...)`.
- Data reused across several test classes goes in the feature's test package: `<Feature>TestDoubles` object (e.g. `UserTestDoubles`) or `<name>Fixture(...)` functions with default parameters (e.g. `notificationFixture()`). Check for these before creating new ones.

## Architecture rules that affect tests

- Value objects and domain models are built in the **application** layer, so use case tests pass primitives/DTOs in and check the mapped domain objects going to the repository.
- IDs are value objects (`UserId`, `UserKeywordId`, ...) from `common/model/id`. Use them, not raw `Long`, wherever the signature does.
- Never wire `domain/seed/` into tests.

## Koin-dependent integration tests

Tests that start the shared Koin container cannot run in parallel. They are excluded from `test` and run in the separate single-fork `koinTest` task (`build.gradle.kts`). Do not write new ones unless the user asks; if you do, add the class to both the `exclude(...)` in `tasks.test` and the `include(...)` in `koinTest`.

## Secrets

Never print or copy values from `.env.*`, `application-*.conf`, or `firebase-service-account.json` into tests. Tests use `application-test.conf` and test doubles (`JWTTestDoubles`).
