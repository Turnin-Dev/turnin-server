package com.peekr.common.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandler() {
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

        exception<BadRequestException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = CommonErrorCode.MalformedRequest.code,
                    message = "${CommonErrorCode.MalformedRequest.description}:\n${cause.message}",
                    status = HttpStatusCode.BadRequest.value,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            val statusCode = call.response.status() ?: HttpStatusCode.InternalServerError
            call.respond(
                status = statusCode,
                message = ErrorResponse(
                    code = UNKNOWN_ERROR_CODE,
                    message = cause.localizedMessage ?: UNKNOWN_ERROR_MESSAGE,
                    status = statusCode.value,
                ),
            )
        }
    }
}

private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
private const val UNKNOWN_ERROR_CODE = "UE001"
