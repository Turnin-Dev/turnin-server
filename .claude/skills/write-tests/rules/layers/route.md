# Route (Endpoint) Tests

Target: `domain/<feature>/presentation/route/*`. **Always use the `TestEndpoint` tools** (`src/test/kotlin/util/TestEndpoint.kt`), never raw Ktor client calls - this is a project rule (README Rule 2).

## Tools

- `testGetEndpoint`, `testPostEndpoint`, `testPatchEndpoint`, `testPutEndpoint`, `testDeleteEndpoint`
- `testPlugin(authRouting = { ... })` installs content negotiation, the exception handler, test JWT auth, and a no-op rate limiter, then mounts the routes under the authenticated route. Use `routing = { ... }` for unauthenticated routes and `role = AuthRole.ADMIN` for admin routes.
- `tokenSubject` is the user ID placed in the test JWT; `null` sends no token.
- `responseValidator = { contains(...) / containsAll(...) / isEmpty() / custom { body, response -> } }`

```kotlin
class ReportRouteTest {
    private val route = Api.V1.Report
    private val usecase: ReportUseCases = mockk()

    @Test
    fun `신고 생성 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }
}
```

## Setup

- Mock the feature's `...UseCases` aggregate; the route test checks HTTP behavior, not business rules.
- Build endpoints from the route constants in `common/route/Api` (`Api.V1.<Feature>`), not hardcoded strings.

## What to cover, per endpoint

1. **Success**: expected status (200 / 201 / 204) and, when there is a body, the fields that matter in it.
2. **401**: no token (`tokenSubject = null`) on authenticated routes.
3. **400**: each request validation the route itself performs - missing or non-numeric path/query parameter, malformed body, each DTO validation rule. One case per rule.
4. **403 / 404 / 409 ...**: each domain exception the use case can throw that maps to a distinct status. Stub the use case to throw it and assert the status **and** the error body (`errorCode.code`, `errorCode.description`).
5. **Unexpected exception**: stub the use case to throw a generic `ApiException` / unexpected error and assert the error body is well-formed.
6. **Parameter handling**: defaults for omitted query parameters, and that parsed values reach the use case (verify with `coVerify` naming those arguments).

Keep each status in its own test. Do not merge 400/401/403.

## What not to do

- Do not re-test business rules here; a mocked use case throwing the exception is enough to test the mapping.
- Rate limits are disabled in `testPlugin` (limit = `Int.MAX_VALUE`), so 429 cannot be tested through it. Do not write rate-limit tests unless the user asks; that needs a custom plugin setup.
- Do not compare the whole response body as one string; assert on the fields that matter so field order and unrelated fields cannot break the test.
