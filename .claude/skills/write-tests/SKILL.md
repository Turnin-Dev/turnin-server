---
name: write-tests
description: Writes or plans tests for turnin-server code (use cases, repository implementations, routes, domain models) following this repo's conventions - Korean backtick test names, JUnit4 + kotlin.test, MockK, TestEndpoint, TestDatabaseFactory. Use when the user asks to write, add, generate, or cover code with tests, or asks which test cases a class or feature needs. Not for just running the existing suite - use the verify skill for that.
argument-hint: "<file-path-or-class> [cases-only]"
---

# Write Tests Skill

Analyze the target, list the test cases it needs, write them, and prove they pass.

**Target:** $ARGUMENTS

Adapted from [mavka-ai/unit-tests-skills](https://github.com/mavka-ai/unit-tests-skills) (MIT) for this repo's Kotlin / Ktor / MockK stack.

## Workflow

Do every step in order. Reading the real classes first is what keeps the tests from failing on wrong constructors or wrong assumptions.

### Step 1: Read rules and context

1. Read `rules/test-case-strategy.md`, `rules/principles.md`, and `rules/project-conventions.md`.
2. Read the layer rule that matches the target (table below).
3. Read the target, then follow its imports: parameter and return types, DTOs, domain models, value objects, enums, exceptions, and the repository/provider interfaces it depends on.
4. Look for an existing `{ClassName}Test` under `src/test/kotlin` (same package path as the target).
   - Found: read it fully. Add only the missing cases to that file, keeping its style.
   - Not found: read two or three neighboring tests in the same feature and layer to match their style.

| Target | Layer rule | Test location |
|---|---|---|
| `domain/<feature>/application/usecase/*` | `rules/layers/usecase.md` | same path under `src/test/kotlin` |
| `domain/<feature>/infrastructure/repository/*Impl` | `rules/layers/repository.md` | same path under `src/test/kotlin` |
| `domain/<feature>/presentation/route/*` | `rules/layers/route.md` | same path under `src/test/kotlin` |
| Domain models, value objects, `common/` utilities | `rules/project-conventions.md` only | same path under `src/test/kotlin` |

### Step 2: List the test cases

Before writing any code, print the list in this format:

```
## Test Cases for {ClassName}

### 1. `{한국어 테스트 이름}`
- **Given:** {preconditions / input state}
- **When:** {action}
- **Then:** {expected outcome}
- **Code branch:** {which path this covers}
```

Cover every branch of the target, including branches inside private functions it calls, and apply the INCLUDE/EXCLUDE rules strictly. If existing tests already cover everything, say so and list what is covered; do not invent cases.

**If the user asked only for the cases** (analysis, coverage review, `cases-only`), stop here. Otherwise continue without waiting; the list stays visible so the tests can be checked against it.

### Step 3: Write the tests

1. Write one test per listed case, following the layer rule and `rules/project-conventions.md`.
2. Add to the existing test class if there is one; never create a duplicate file.
3. Reuse existing fixtures (`<Feature>TestDoubles`, `<feature>Fixture()` helpers, `util/` tools) before creating new ones.
4. If you add or drop a case compared with the Step 2 list, say which and why.

### Step 4: Verify

Follow `rules/verification.md`: compile, run only the new test class, fix the tests (never production code) until they pass, then lint. Report the result of each.

## Rules reference

- `rules/test-case-strategy.md` - what to include and exclude
- `rules/principles.md` - what makes a test good (structure, focus, mocks, test data)
- `rules/project-conventions.md` - naming, libraries, fixtures, file placement for this repo
- `rules/layers/usecase.md` - use case tests with MockK
- `rules/layers/repository.md` - repository tests with `TestDatabaseFactory` / `PostgresRule`
- `rules/layers/route.md` - endpoint tests with `TestEndpoint`
- `rules/verification.md` - compile, run, fix, lint

## Troubleshooting

- **Target not found:** report the exact path searched and ask.
- **Tests fail because production code behaves differently than expected:** do not change production code. Make the test match the current behavior. If the behavior looks like a bug, add `// NOTE: 현재 동작은 버그일 수 있음 - {설명}` and report it.
- **Docker errors in a `PostgresRule` test:** tell the user to start Docker; do not debug the test logic.
