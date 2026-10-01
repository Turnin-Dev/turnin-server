# Verification

Tests that do not compile or do not pass are not deliverable. Run these in order.

## 1. Compile

```bash
./gradlew testClasses -q
```

Fix errors (imports, constructor arguments, types) and rerun. Stop after 5 failed attempts, show the remaining errors, and ask.

## 2. Run only the new test class

```bash
./gradlew test --tests "com.turnin.<package>.<TestClassName>" -x koinTest
```

- `-x koinTest` skips the separate Koin task that `test` normally triggers; it is not needed for a single class.
- Confirm the tests actually executed: check `build/test-results/test/TEST-<fully.qualified.Name>.xml` for the expected `tests=` count with `failures="0" errors="0"`. A fast green build can mean nothing ran.
- `PostgresRule` tests need Docker. On a container/connection error, tell the user to start Docker.

## 3. Fix failures in the test, not in production code

- Read the failure, find the cause (wrong expected value, missing stub, wrong assumption about behavior), fix the test, rerun.
- Never modify production code to make a test pass.
- If the production behavior looks like a bug: make the test document the current behavior, add `// NOTE: 현재 동작은 버그일 수 있음 - {설명}`, and report it.
- If a test still fails after 3 attempts, keep it in the file with `@Ignore("{무엇을 검증하는지, 해결하지 못한 실패 내용}")` (`org.junit.Ignore`) and report it explicitly: test name, what it asserts, the actual failure, and whether you think it is a production bug or a wrong assumption.

## 4. Lint

```bash
./gradlew ktlintCheck
```

Fix violations by hand in the files you wrote (import order, indentation, trailing commas). Do not run `ktlintFormat` unless the user asks. The pre-commit hook runs ktlint too.

## 5. Report

State: test cases written (count, against the Step 2 list), compile result, test result with counts, lint result, and any `@Ignore`d test or suspected production bug. For a final full-suite check before review, use the `verify` skill.
