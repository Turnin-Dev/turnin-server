# Verification

Tests that do not compile or do not pass are not deliverable. Run these in order.

## 1. Compile

```bash
./gradlew testClasses -q
```

Fix errors (imports, constructor arguments, types) and rerun. Stop after 5 failed attempts, show the remaining errors,
and ask.

## 2. Run only the new test class

```bash
./gradlew test --tests "com.turnin.<package>.<TestClassName>" -x koinTest
```

- `-x koinTest` skips the separate Koin task that `test` normally triggers; it is not needed for a single class.
- Confirm the tests actually executed: check `build/test-results/test/TEST-<fully.qualified.Name>.xml` for the expected
  `tests=` count with `failures="0" errors="0"`. A fast green build can mean nothing ran.
- `PostgresRule` tests need Docker. On a container/connection error, tell the user to start Docker.

## 3. Investigate failures; keep contract-based expectations

- Read the failure and find the cause before changing anything.
- If the test itself is wrong (missing stub, wrong setup, wrong constructor arguments, an assumption the contract does
  not support), fix the test and rerun.
- Expected values come from the contract (`docs/spec/`, KDoc, API docs), not from what the code currently returns.
  Never change an expected value just to match the implementation.
- Never modify production code to make a test pass.
- If the implementation differs from the contract: keep the expectation, leave the test failing, and report it as an
  implementation defect (test name, the contract it is based on, expected vs. actual).
- If a test still fails after 3 attempts, stop and report it as unresolved instead of submitting. Never disable it with
  `@Ignore` or any other means to make the test task pass. Report explicitly: test name, what it asserts, the actual
  failure, and whether you think it is a production bug or a wrong assumption.

## 4. Lint

```bash
./gradlew ktlintCheck
```

Fix violations by hand in the files you wrote (import order, indentation, trailing commas). Do not run `ktlintFormat`
unless the user asks. The pre-commit hook runs ktlint too.

## 5. Report

State: test cases written (count, against the Step 2 list), compile result, test result with counts, lint result, and
any test left failing or unresolved, with the suspected implementation defect. For a final full-suite check before
review, use the `verify` skill.
