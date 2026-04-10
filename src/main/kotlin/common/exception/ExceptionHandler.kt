package com.peekr.common.exception

import com.peekr.common.db.DatabaseErrorMessage
import com.peekr.common.db.DatabaseException
import com.peekr.common.db.toHttpStatusCode
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.LogTag
import com.peekr.common.util.LogType
import com.peekr.common.validator.ValidatorException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond

private const val TAG_URL = "request_url"
private const val TAG_METHOD = "request_method"
private const val TAG_IP = "client_ip"
private const val TAG_EXCEPTION = "exception_type"
private const val TAG_STATUS = "status_code"
private const val TAG_ERROR_CODE = "error_code"

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<DatabaseException> { call, cause ->
            warnLogging(call, "DatabaseException", cause)
            call.respond(
                status = cause.toHttpStatusCode(),
                message = ErrorResponse(
                    code = DatabaseErrorMessage.CLIENT_COMMON_CODE,
                    message = cause.message ?: DatabaseErrorMessage.CLIENT_COMMON_MESSAGE,
                    status = cause.toHttpStatusCode().value,
                ),
            )
        }

        exception<ApiException> { call, cause ->
            warnLogging(call, "ApiException", cause)
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
            warnLogging(call, "ValidatorException", cause)
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
            warnLogging(call, "DomainException", cause)
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
            warnLogging(call, "IllegalArgumentException", cause)
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
            warnLogging(call, "BadRequestException", cause)
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
            errorLogging(call, statusCode, cause)
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

private fun warnLogging(
    call: ApplicationCall,
    tag: String,
    cause: Throwable,
    errorCode: ApiErrorCode? = null,
) {
    val tags = mutableMapOf(
        TAG_URL to call.request.uri,
        TAG_METHOD to call.request.httpMethod.value,
        TAG_IP to call.request.origin.remoteHost,
        TAG_EXCEPTION to tag,
        LogTag.LOG_TYPE.key to LogType.NORMAL.value,
    )

    errorCode?.let { tags[TAG_ERROR_CODE] = it.code }

    val errorCodeMsg = if (errorCode != null) "(${errorCode.code}) " else ""
    LOGGER.warn("[$tag]$errorCodeMsg ${cause.message}", tags, cause)
}

private fun errorLogging(
    call: ApplicationCall,
    statusCode: HttpStatusCode,
    cause: Throwable,
) {
    val tags = mapOf(
        TAG_URL to call.request.uri,
        TAG_METHOD to call.request.httpMethod.value,
        TAG_IP to call.request.origin.remoteHost,
        TAG_STATUS to statusCode.value.toString(),
        TAG_EXCEPTION to "CRITICAL_ERROR",
        LogTag.LOG_TYPE.key to LogType.NORMAL.value,
    )

    LOGGER.error(
        message = "[Unhandled Exception] ${cause.message}",
        tags = tags,
        e = cause,
    )
}
