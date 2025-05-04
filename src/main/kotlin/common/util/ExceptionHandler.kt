package com.peekr.common.util

import com.peekr.exception.ApiException
import com.peekr.exception.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message =
                    ErrorResponse(
                        code = cause.code,
                        message = cause.message,
                        status = cause.status.value,
                    ),
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message =
                    ErrorResponse(
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
