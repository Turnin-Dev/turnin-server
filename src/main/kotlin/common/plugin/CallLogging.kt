package com.turnin.common.plugin

import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.log.LogSanitizer
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.clientIp
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

/** CallLogging을 이용한 로그 설정 */
fun Application.configureCallLogging() {
    val appConfig by inject<AppConfig>()
    val isDevelopment = appConfig.getOrDefault("ktor.development", "false") == "true"

    // RateLimit이 헤더 위조로 우회될 수 있는 문제:
    // 서버/인프라 레벨에서 조치.
    // - Nginx에 Cloudflare Authenticated Origin Pulls(전역) 적용
    // - Origin(AWS)이 Cloudflare를 거치지 않은 연결을 TLS 핸드셰이크 단계에서 거부하도록 설정
    // - 따라서 헤더 위조를 통한 origin 직접 접근 자체가 인프라 단에서 차단되어,
    //  애플리케이션 레벨의 신뢰 프록시 검증은 현재 우선순위 낮음으로 보류
    // - 관련 참고: AOP 설정 문서 등
    install(XForwardedHeaders)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }

        mdc(LogTag.IP.key) { call -> call.clientIp() }
        mdc(LogTag.IP_MASKED.key) { call -> maskIp(call.clientIp()) }

        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            val path = maskPath(call.request.path())
            val queryParams =
                call.request.queryParameters
                    .entries()
                    .joinToString(", ") { "${it.key}=${it.value}" }
            val duration = call.processingTimeMillis()
            val clientIp = maskIp(call.clientIp())

            if (isDevelopment) {
                // 개발 환경: 색상 + 멀티라인
                val coloredStatus =
                    when {
                        status == null -> "\u001B[33mUNKNOWN\u001B[0m"
                        status.value < 300 -> "\u001B[32m$status\u001B[0m"
                        status.value < 400 -> "\u001B[33m$status\u001B[0m"
                        else -> "\u001B[31m$status\u001B[0m"
                    }
                val coloredMethod = "\u001B[36m$httpMethod\u001B[0m"
                """
        |
        |------------------------ Request Details ------------------------
        |Status: $coloredStatus
        |Method: $coloredMethod
        |Path: $path
        |Query Params: $queryParams
        |Remote Host: $clientIp
        |User Agent: $userAgent
        |Duration: ${duration}ms
        |------------------------------------------------------------------
        |
                """.trimMargin()
            } else {
                // 운영 환경: 색상 없음 + 단일 라인
                val statusValue = status?.value ?: "UNKNOWN"
                "status=$statusValue " +
                    "method=$httpMethod " +
                    "path=${LogSanitizer.sanitize(path)} " +
                    "query=\"${LogSanitizer.sanitize(queryParams)}\" " +
                    "remote=${LogSanitizer.sanitize(clientIp)} " +
                    "ua=\"${LogSanitizer.sanitize(userAgent)}\" " +
                    "duration=${duration}ms"
            }
        }
    }
}

/**
 * 경로 마스킹 규칙
 * - Pair(정규식, 마스킹 변환 함수)
 * - 새 경로 추가 시 여기에만 추가
 */
private val pathMaskingRules: List<Pair<Regex, (MatchResult) -> String>> = listOf(
    // /auth/exists/provider/{provider}/{providerId} → providerId 마스킹
    Regex("/auth/exists/provider/([^/]+)/[^/]+") to
        { match -> "/auth/exists/provider/${match.groupValues[1]}/***" },
    // /auth/exists/displayId/{displayId} → displayId 마스킹
    Regex("/auth/exists/displayId/[^/]+") to
        { _ -> "/auth/exists/displayId/***" },
)

/** 경로 마스킹 */
private fun maskPath(path: String): String {
    var masked = path
    pathMaskingRules.forEach { (regex, transform) ->
        masked = regex.replace(masked, transform)
    }
    return masked
}

/** IP 마스킹 - 가장 마지막 옥텟 마스킹 */
private fun maskIp(ip: String): String = when {
    ip.isBlank() -> {
        "unknown"
    }

    ip.contains(":") -> {
        val split = ip.split(":")
        val head = split.dropLast(2)
        head.joinToString(":") + ":xxxx:xxxx"
    }

    else -> {
        ip.replaceAfterLast(".", "***")
    }
}
