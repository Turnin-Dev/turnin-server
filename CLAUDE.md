# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Kotlin 2.1.10 + Ktor 3.1.2 (Netty engine), Gradle Kotlin DSL, JDK 17. Exposed 0.61.0 (ORM) over PostgreSQL, Koin 4.0.3 + KSP for DI, Flyway for migrations, MockK + Testcontainers (Postgres) for tests.

## Architecture

Single Gradle module. Package-based modular monolith with Clean Architecture per feature (`presentation → application → domain ← infrastructure`). Top-level packages: `common/` (cross-cutting: di, jwt, db, batch, exception, firebase, ml, plugin, route, util) and `domain/<feature>/` (account, announcement, auth, block, discover, feed, file, friend, keyword, notification, report, user, userKeyword).

Full layering conventions, folder structure per layer, and dependency-direction rules are documented in @README.md — read it before adding a new feature or layer.

`domain/seed/` is a one-off seed-data insertion module — never wire it into normal application code, it exists only for initial data seeding (see its own README).

## Commands

- Build: `./gradlew build` (includes ktlintCheck + tests)
- Run locally: `./gradlew runDev` (loads `.env.dev` + `application-dev.conf`) or `./gradlew runProd` (prod equivalent)
- Lint: `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
- Fat jar for deploy: `./gradlew buildFatJar` → `turnin-api.jar`
- Tests: `./gradlew test` — this also triggers `koinTest` (finalizedBy), a separate task that runs `HardDeleteExpiredAccountsUseCaseIntegrationTest` and `AccountDeletionIntegrationTest` in one Gradle test process (`maxParallelForks = 1`) because they share Koin DI container state. The regular `test` task allows up to `Runtime.getRuntime().availableProcessors()` Gradle test processes for the other tests. Don't assume `koinTest` ran just because `test` passed — check both task results.
- Integration tests use Testcontainers (Postgres) — **Docker must be running locally** or these tests fail immediately.

## Conventions

- Branch flow: feature branches → PR into `develop`; `develop` → `main` for releases (version-bump commits like `chore: 1.5.0로 버전업`).
- Commit messages: Korean, Conventional-Commits-style prefixes (`feat:`, `fix:`, `refactor:`, `chore:`, `test:`, `chore(deps):`).
- Endpoint tests use a custom `TestEndpoint` test tool (see existing tests under `src/test/kotlin` for the pattern) rather than raw Ktor test client calls.

## Secrets

- `.env.dev`, `.env.localtest`, `application-*.conf`, and `firebase-service-account.json` contain real credentials/config. Never print, echo, or commit their contents — reference key names only.
- Keep `.env.prod` separately


## Leave alone

These are generated or local-only and shouldn't be committed or regenerated unless explicitly asked: `docs/` (Swagger-Codegen-generated API docs), `seed/` (seed data files), `logs/`, `hs_err_pid*.log`, `opentelemetry-javaagent.jar` (optional local javaagent override).
