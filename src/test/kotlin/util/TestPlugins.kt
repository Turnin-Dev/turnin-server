package com.peekr.util

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

/**
 * 테스트 유틸로서, 모듈/플러그인/라우팅 등을 설정할 수 있다.
 *
 * @param module Koin 모듈
 * @param plugin 플러그인 (Application 범위)
 * @param routing 라우터 (Routing 범위)
 * @param routingApplicationScope 라우터 (Application 범위)
 */
fun ApplicationTestBuilder.testPlugin(
    module: Module? = null,
    plugin: Application.() -> Unit = {},
    routing: Routing.() -> Unit = {},
    routingApplicationScope: Application.() -> Unit = {},
) {
    module?.let {
        testKoinModule(module = module)
    }
    application {
        testContentNegotiation()
        testExceptionHandler()
        plugin()
        routing {
            routing()
        }
        routingApplicationScope()
    }
}

private fun Application.testContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}

private fun ApplicationTestBuilder.testKoinModule(module: Module) {
    install(Koin) {
        modules(module)
    }
}

private fun Application.testExceptionHandler() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message = ErrorResponse(
                    code = cause.errorCode.code,
                    message = cause.message,
                    status = cause.status.value,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code = INTERNAL_SERVER_ERROR_CODE,
                    message = cause.localizedMessage ?: UNKNOWN_ERROR_MESSAGE,
                    status = HttpStatusCode.InternalServerError.value,
                ),
            )
        }
    }
}

private const val INTERNAL_SERVER_ERROR_CODE = "INTERNAL_SERVER_ERROR"
private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
