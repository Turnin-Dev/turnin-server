package com.turnin.common.util.log

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin

/**
 * 클라이언트 IP 추출
 */
fun ApplicationCall.clientIp(): String =
    request.headers["CF-Connecting-IP"] // Cloudflare 실제 클라이언트 IP
        ?: request.headers["X-Forwarded-For"]
            ?.split(",")
            ?.first()
            ?.trim()
            ?.takeIf { it.isNotEmpty() } // fallback 1
        ?: request.origin.remoteHost // fallback 2
