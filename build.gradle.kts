plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
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

    filter {
        exclude("**/build/generated/ksp/**")
        exclude("**/build/generated/source/ksp/**")
        exclude("**/build/generated/ksp/main/kotlin/**")
    }
}

tasks.withType<JavaExec> {
    val configFile = System.getProperty("config.file")
    if (configFile != null) {
        systemProperty("config.file", configFile)
    }

    // 자바 에이전트 경로
    jvmArgs(
        "-javaagent:" +
            "/Users/kwagsangjin/Documents/MyProjects/Peekr/Backend/release/peekr-server/opentelemetry-javaagent.jar",
    )

    // 환경 변수 주입
    // TODO: 액세스 키 따로 관리 예정
    environment("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:5080/api/default")
    environment("OTEL_EXPORTER_OTLP_HEADERS", "Authorization=Basic a3NqMTIzQHRlc3QuY29tOnlzZ3Jnc1JCQ1Z6MkZWM1g=")

    // 리소스 속성 (프로세스 상세 정보 수집 제외)
    environment(
        "OTEL_RESOURCE_ATTRIBUTES",
        "service.name=peekr-backend," +
            "telemetry.sdk.name=otel," +
            "telemetry.sdk.language=java",
    )

    // 프로토콜 및 내보내기 설정
    environment("OTEL_EXPORTER_OTLP_PROTOCOL", "http/protobuf")
    environment("OTEL_LOGS_EXPORTER", "otlp")
    environment("OTEL_TRACES_EXPORTER", "otlp")
    environment("OTEL_METRICS_EXPORTER", "none")

    // 불필요한 시스템 리소스 수집기 비활성화
    environment("OTEL_JAVAAGENT_DISABLED_RESOURCE_PROVIDERS", "os,process")
    // HikariCP 히스토그램 메트릭 비활성화 (메트릭 none이므로 주석 처리)
//    environment("OTEL_INSTRUMENTATION_HIKARICP_ENABLED", "false")
    // JVM 커맨드 인자 수집 비활성화
    environment(
        "OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS",
        "io.opentelemetry.instrumentation.resources.ProcessResourceProvider," +
            "io.opentelemetry.instrumentation.resources.JarServiceNameDetector",
    )

    // 부가 메트릭 수집 주기 (메트릭 none이므로 주석 처리)
//    environment("OTEL_METRIC_EXPORT_INTERVAL", "60000")
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

// tasks.test {
//    systemProperty("config.file", "application-test.conf")
//    useJUnitPlatform()
// }

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
