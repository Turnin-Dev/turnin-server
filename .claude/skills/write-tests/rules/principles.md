# Test Principles

A good test is **clear** (understood in 10 seconds), **complete** (everything the assertion depends on is visible in the test), **concise** (irrelevant setup is hidden), and **resilient** (fails only when the tested behavior breaks).

## 1. given / when / then

Every test has the three sections, marked with the comments this repo uses: `// given`, `// when`, `// then` (or `// when, then` for exception checks).

```kotlin
@Test
fun `질문 길이가 2200자를 초과하면 예외가 발생한다`() = runTest {
    // given
    val tooLongQuestion = "a".repeat(2201)

    // when, then
    assertThrows<PingPongException.QuestionTooLongException> {
        usecase(TestUserId, TestUserKeywordId, tooLongQuestion)
    }
}
```

## 2. One scenario per test

- One behavior, one cause, one reason to fail. Several assertions are fine when together they check that one behavior.
- Split the test when: the name needs "그리고", there are two `// when` sections, or state changes between assertions.
- Test behaviors, not methods. If one call has three observable effects (saves, sends a notification, returns an ID), a failure in one should not hide the others; give independently breakable effects their own tests.

## 3. Keep cause and effect together

- Values the assertion depends on are written inside the test, even if they equal a fixture default.
- `@Before` is for infrastructure only (DB init, creating the subject). Test-specific data goes in the test.
- Shared `Test...` constants are fine for values the assertion does not depend on.

```kotlin
// Bad: 5 comes from somewhere else
assertEquals(EXPECTED_COUNT, result.size)

// Good
val expectedCount = 5
coEvery { repository.getReportReasons() } returns List(5) { TestReportReason }
...
assertEquals(expectedCount, result.size)
```

## 4. No logic in tests

- No `if`, loops, or computed expected values in assertions. Write literals.
- `repeat(n) { insert... }` for setup is fine; computing the expected value from it is not.
- Simple and repetitive beats clever. KISS over DRY.

```kotlin
// Bad: hides a bug in the concatenation
assertEquals(baseUrl + "/u/0/photos", actualUrl)

// Good
assertEquals("https://example.com/u/0/photos", actualUrl)
```

## 5. Deterministic

- Never assert against "now". Pass fixed timestamps in, or assert on relationships that cannot race (not null, before/after a fixed value).
- No dependence on test order or on data left by another test.

## 6. Test through public API

- Private functions are covered through the public function that calls them.
- Do not verify how the result was computed, only what it is.

## 7. Mocks: verify only what matters

- Mock dependencies (repository and provider interfaces). Never mock the class under test, value objects, DTOs, or domain models; build real ones.
- Stubbing and verification are different jobs:
  - In `coEvery { }`, `any()` is fine. A stub only decides what the mock returns.
  - In `coVerify { }`, name the arguments this test is about. `any()` there asserts nothing about them. Use `any()` only for arguments irrelevant to this test.
- To check fields of an object passed to a dependency, capture it with `slot<T>()` and assert on the fields that matter.
- Do not stub calls the scenario never reaches, and do not verify every interaction "to be safe".

```kotlin
// then
val saved = slot<PingPong>()
coVerify(exactly = 1) { pingPongRepository.save(capture(saved)) }
assertEquals("질문 내용", saved.captured.question)
```

## 8. Test data

- Use helper functions or fixtures with defaults so each test states only the fields it cares about.
- If the test depends on a value, set it explicitly in the test even when it matches the default.
- Helpers contain no business logic.
