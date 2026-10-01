# Use Case Tests

Target: `domain/<feature>/application/usecase/*UseCase`. Pure unit tests: real use case, mocked dependencies, no DB, no Ktor.

## Setup

- Mock every constructor dependency (repository interfaces from `domain/.../domain/repository`, provider interfaces from `domain/.../domain/provider`).
- Construct the use case directly. No Koin.

```kotlin
class CreateReportUseCaseTest {
    private val reportRepository = mockk<ReportRepository>()
    private val usecase = CreateReportUseCase(reportRepository)

    @Test
    fun `신고 대상이 전부 없는 경우 도메인 예외가 발생한다`() = runTest {
        // given
        val reportDetailDto = TestReportDetailDto.copy(
            reportedId = null,
            reportedUserKeywordId = null,
        )

        // when, then
        assertThrows<ReportException.MissingReportTargetException> {
            usecase(TestReporterId, reportDetailDto)
        }
    }
}
```

## What to cover

- Each business rule branch: every `throw`, every early return, every conditional path.
- Authorization rules inside the use case (owner only, requester must equal target, ...): one case per rule.
- Mapping to domain objects: when the use case builds a domain model / value object and hands it to a repository, capture it with `slot<T>()` and assert the fields that matter.
- Side effects through providers (notifications etc.): verify they happen, and verify they do **not** happen on the failure path (`coVerify(exactly = 0) { ... }`).
- Failure tolerance, when the code has it: e.g. "알림 전송 실패 시에도 상태 수정은 성공으로 처리한다" - stub the provider to throw and assert the main result still succeeds.

## What not to do

- Do not re-test repository queries or value object validation that has its own test; one case showing the use case surfaces the failure is enough.
- Do not stub with exact arguments when the test is not about them; `any()` in `coEvery` keeps stubs from breaking for unrelated reasons.
- Use cases that open `suspendTransaction` themselves need a database; check how neighboring tests of similar use cases handle it before choosing between mocks and `TestDatabaseFactory`.
