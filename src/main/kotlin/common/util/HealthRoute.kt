package com.turnin.common.util

import com.turnin.common.db.suspendTransaction
import com.turnin.common.route.Api
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import java.time.Duration
import java.time.Instant

fun Route.healthRoutes(route: Api.Health) {
    val startTime = PeekrDateTime.now()

    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Health Check API"
    }) {
        get({ healthDocs() }) {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        get(route.DETAIL, { healthDetailDocs() }) {
            val dbOk = runCatching {
                suspendTransaction {
                    exec("SELECT 1")
                }
                true
            }.getOrDefault(false)

            val uptime = formatUptime(startTime)
            val status = if (dbOk) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

            call.respond(
                status,
                mapOf(
                    "status" to if (dbOk) "ok" else "degraded",
                    "database" to if (dbOk) "ok" else "error",
                    "uptime" to uptime,
                ),
            )
        }
    }
}

private fun formatUptime(startTime: Instant): String {
    val seconds = Duration.between(startTime, PeekrDateTime.now()).seconds
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return "${hours}h ${minutes}m ${secs}s"
}

private fun RouteConfig.healthDocs() {
    summary = "서버 상태 확인"
    description = "서버의 상태를 확인한다."
}

private fun RouteConfig.healthDetailDocs() {
    summary = "서버 상태 상세정보 확인"
    description = "서버의 상태와 DB의 상태를 확인한다."
}
