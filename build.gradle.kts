import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
}

group = "com.turnin"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("turnin-api.jar")
    }
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

    filter {
        exclude("**/build/generated/ksp/**")
        exclude("**/build/generated/source/ksp/**")
        exclude("**/build/generated/ksp/main/kotlin/**")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    exclude("firebase-service-account.json")
    exclude("model_int8.onnx")
    exclude("tokenizer.json")
    exclude("tokenizer_config.json")
}

tasks.withType<JavaExec> {
    val configFile = System.getProperty("config.file")
    if (configFile != null) {
        systemProperty("config.file", configFile)
    }
    // 로컬에 에이전트 파일 있을 때만 적용
    val agentFile = rootProject.file("opentelemetry-javaagent.jar")
    if (agentFile.exists()) {
        jvmArgs("-javaagent:${agentFile.absolutePath}")
    }
}

fun loadDotenv(environment: String): Map<String, String> {
    val dotenvFile = rootProject.file(".env.$environment")
    if (!dotenvFile.exists()) return emptyMap()

    return dotenvFile
        .readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .associate { it.substringBefore("=") to it.substringAfter("=") }
}

val envDev = loadDotenv("dev")
val envProd = loadDotenv("prod")

tasks.register<JavaExec>("runDev") {
    group = "application"
    description = "Run the application in development mode"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.ktor.server.netty.EngineMain")
    systemProperty("config.resource", "application-dev.conf")
    systemProperty("io.ktor.development", "true")
    systemProperty("logback.configurationFile", "logback-dev.xml")
    envDev.forEach { (key, value) ->
        environment(key, value)
    }
}

tasks.register<JavaExec>("runDevWithOTel") {
    group = "application"
    description = "Run the application in development mode"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.ktor.server.netty.EngineMain")
    systemProperty("config.resource", "application-dev.conf")
    systemProperty("io.ktor.development", "true")
    systemProperty("logback.configurationFile", "logback-prod.xml")
    envDev.forEach { (key, value) ->
        environment(key, value)
    }
}

tasks.register<JavaExec>("runProd") {
    group = "application"
    description = "Run the application in production mode"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.ktor.server.netty.EngineMain")
    systemProperty("config.resource", "application-prod.conf")
    systemProperty("io.ktor.development", "false")
    systemProperty("logback.configurationFile", "logback-prod.xml")
    envProd.forEach { (key, value) -> environment(key, value) }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.h2)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)

    // Test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.client.content.negotiation)
    // Test (Mockk)
    testImplementation(libs.mockk)
    // Test (Client)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio)

    // Status Pages
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)

    // Open API
    implementation(libs.ktor.server.resources)
    implementation(libs.smiley4.swagger.ui)
    implementation(libs.smiley4.openapi)

    // CORS
    implementation(libs.ktor.server.cors)

    // PostgreSQL JDBC Driver
    implementation(libs.postgresql)
    testImplementation(libs.test.container.postgresql)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.datatime)

    // HikariCP (Connection Pool)
    implementation(libs.hikariCP)

    // dot env
    implementation(libs.dotenv)

    // JWT
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.jwt)

    // Logging
    implementation(libs.ktor.server.call.logging)
    implementation(libs.logback.classic)

    // DI
    implementation(libs.koin)
    implementation(libs.koin.core)
    implementation(libs.koin.logger)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    // Flyway
    implementation(libs.flyway.core)

    // AWS S3
    implementation(libs.amazon.awssdk.s3)

    // ONNX, Tokenizers
    implementation(libs.onnx.runtime)
    implementation(libs.huggingface.tokenizers)

    // Firebase Admin
    implementation(libs.firebase.admin) {
        exclude(group = "io.netty")
    }

    // OpenTelemetry
    implementation(libs.opentelemetry.logback.appender)
}
