package com.peekr.common.exception

import com.peekr.common.db.DatabaseErrorMessage
import com.peekr.common.db.DatabaseException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.validator.ValidatorException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<DatabaseException> { call, cause ->
            warnLogging("DatabaseException", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code = DatabaseErrorMessage.CLIENT_COMMON_CODE,
                    message = DatabaseErrorMessage.CLIENT_COMMON_MESSAGE,
                    status = HttpStatusCode.InternalServerError.value,
                ),
            )
        }

        exception<ApiException> { call, cause ->
            warnLogging(cause)
            call.respond(
                status = cause.status,
                message = ErrorResponse(
                    code = cause.errorCode.code,
                    message = cause.errorCode.description,
                    status = cause.status.value,
                ),
            )
        }

        exception<ValidatorException> { call, cause ->
            warnLogging("ValidatorException", cause)
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = CommonErrorCode.ValidationDefault.code,
                    message = cause.message ?: CommonErrorCode.ValidationDefault.description,
                    status = HttpStatusCode.BadRequest.value,
                ),
            )
        }

        exception<DomainException> { call, cause ->
            warnLogging("DomainException", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code = CommonErrorCode.DomainError.code,
                    message = cause.message ?: CommonErrorCode.DomainError.description,
                    status = HttpStatusCode.InternalServerError.value,
                ),
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            warnLogging("IllegalArgumentException", cause)
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = CommonErrorCode.MalformedRequest.code,
                    message = cause.message ?: CommonErrorCode.MalformedRequest.description,
                    status = HttpStatusCode.BadRequest.value,
                ),
            )
        }

        exception<BadRequestException> { call, cause ->
            warnLogging("BadRequestException", cause)
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = CommonErrorCode.MalformedRequest.code,
                    message = CommonErrorCode.MalformedRequest.description,
                    status = HttpStatusCode.BadRequest.value,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            val statusCode = call.response.status() ?: HttpStatusCode.InternalServerError
            errorLogging(statusCode, cause)
            call.respond(
                status = statusCode,
                message = ErrorResponse(
                    code = UNKNOWN_ERROR_CODE,
                    message = UNKNOWN_ERROR_MESSAGE,
                    status = statusCode.value,
                ),
            )
        }
    }
}

private const val UNKNOWN_ERROR_MESSAGE = "서버에서 알 수 없는 에러가 발생했습니다."
private const val UNKNOWN_ERROR_CODE = "UEC001"

private val LOGGER = AppLoggerFactory.createLogger("ExceptionHandler")

private fun warnLogging(cause: ApiException) {
    LOGGER.warn(
        "[ApiException] " +
            "code=${cause.errorCode.code}, " +
            "status=${cause.status.value}, " +
            "message=${cause.message}",
        cause,
    )
}

private fun warnLogging(tag: String, cause: Throwable) {
    LOGGER.warn(
        "[$tag] " +
            "message=${cause.message}",
        cause,
    )
}

private fun errorLogging(statusCode: HttpStatusCode, cause: Throwable) {
    LOGGER.error(
        cause,
        "[Unhandled Throwable] " +
            "status=${statusCode.value}, " +
            "code=${UNKNOWN_ERROR_CODE}, " +
            "message=${UNKNOWN_ERROR_MESSAGE}",
    )
}
