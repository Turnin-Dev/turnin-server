---
name: run-dev
description: Launches the turnin-server Ktor application in local development mode. Use when the user wants to run, start, or try out the server locally.
---

Run `./gradlew runDev` from the project root.

This custom Gradle task (defined in `build.gradle.kts`):
- Loads environment variables from `.env.dev`
- Uses `application-dev.conf` as the Ktor config resource
- Uses `logback-dev.xml` for logging
- Enables `io.ktor.development=true` (hot reload)

The server needs its usual local dependencies available (Postgres reachable per `application-dev.conf`, etc.) — if startup fails, check the error for which dependency is missing before assuming a code bug.

Do not use `./gradlew runProd` unless the user explicitly asks to run in production mode — it loads `.env.prod` and real production-pointed config.
