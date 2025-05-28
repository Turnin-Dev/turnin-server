plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktlint)
}

group = "com.peekr"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

ktlint {
    verbose.set(true)
    android.set(false) // Android 프로젝트가 아니라면 false
    outputToConsole.set(true)

    // Reports (예: GitHub Actions용)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.client.content.negotiation)

    // Status Pages
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)

    // Open API
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.resources)

    // PostgreSQL JDBC Driver
    implementation(libs.postgresql)

    // Exposed ORM
    implementation(libs.exposed.orm.core)
    implementation(libs.exposed.orm.dao)
    implementation(libs.exposed.orm.jdbc)

    // HikariCP (Connection Pool)
    implementation(libs.hikariCP)

    // dot env
    implementation(libs.dotenv)

    // JWT
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.jwt)

    // Logging
    implementation(libs.ktor.server.call.logging)

    // DI
    implementation(libs.koin)
    implementation(libs.koin.logger)

    // Flyway
    implementation(libs.flyway.core)
}
