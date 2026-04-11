package com.peekr.common.plugin

import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.log.LogSanitizer
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

/** CallLogging을 이용한 로그 설정 */
fun Application.configureCallLogging() {
    val appConfig by inject<AppConfig>()
    val isDevelopment = appConfig.getOrDefault("ktor.development", "false") == "true"

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            val path = call.request.path()
            val queryParams =
                call.request.queryParameters
                    .entries()
                    .joinToString(", ") { "${it.key}=${it.value}" }
            val duration = call.processingTimeMillis()
            val remoteHost = call.request.origin.remoteHost

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
        |Remote Host: $remoteHost
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
                    "remote=${LogSanitizer.sanitize(remoteHost)} " +
                    "ua=\"${LogSanitizer.sanitize(userAgent)}\" " +
                    "duration=${duration}ms"
            }
        }
    }
}
