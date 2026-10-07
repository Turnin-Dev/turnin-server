---
name: verify
description: Runs ktlintCheck and the full Gradle test suite (including the separate koinTest and postgresTest tasks) for turnin-server, then reports pass/fail. Use after making code changes in this repo, before telling the user the change is done or ready for review.
---

Run these in order and report the result of each:

1. `./gradlew ktlintCheck` — lint check. On failure, list the violations; do not auto-run `ktlintFormat` unless the user asks.
2. `./gradlew test` — runs the standard parallel test suite, then (via `finalizedBy`) automatically runs two more tasks: `koinTest` (Koin integration tests, one process) and `postgresTest` (`PostgresRule` classes listed in `postgresTestPatterns`, kept apart from H2 tests). All three are part of this one command, but check `Task :test`, `Task :koinTest`, and `Task :postgresTest` results separately — a green `test` task does not by itself confirm the other two passed.
3. Integration tests require Docker to be running locally (Testcontainers spins up Postgres). If tests fail with a Docker/container-connection error, tell the user to start Docker rather than debugging the test logic.

Summarize: lint result, test result (all three tasks), and any failing test names with their error output.
