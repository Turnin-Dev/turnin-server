package com.peekr.plugin

import com.peekr.exception.ApiException
import com.peekr.exception.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message = ErrorResponse(
                    code = cause.code,
                    message = cause.message,
                    status = cause.status.value
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code = InternalServerErrorCode,
                    message = cause.localizedMessage ?: UnknownErrorMessage,
                    status = HttpStatusCode.InternalServerError.value
                )
            )
        }
    }
}

private const val InternalServerErrorCode = "INTERNAL_SERVER_ERROR"
private const val UnknownErrorMessage = "Unknown error occurred"