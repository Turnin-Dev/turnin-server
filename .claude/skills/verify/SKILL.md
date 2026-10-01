---
name: verify
description: Runs ktlintCheck and the full Gradle test suite (including the separate koinTest task) for turnin-server, then reports pass/fail. Use after making code changes in this repo, before telling the user the change is done or ready for review.
---

Run these in order and report the result of each:

1. `./gradlew ktlintCheck` — lint check. On failure, list the violations; do not auto-run `ktlintFormat` unless the user asks.
2. `./gradlew test` — runs the standard parallel test suite, then (via `finalizedBy`) automatically runs the `koinTest` task, which runs `HardDeleteExpiredAccountsUseCaseIntegrationTest` and `AccountDeletionIntegrationTest` single-threaded. Both are part of this one command, but check the Gradle output for both `Task :test` and `Task :koinTest` results separately — a green `test` task does not by itself confirm `koinTest` passed.
3. Integration tests require Docker to be running locally (Testcontainers spins up Postgres). If tests fail with a Docker/container-connection error, tell the user to start Docker rather than debugging the test logic.

Summarize: lint result, test result (both tasks), and any failing test names with their error output.
