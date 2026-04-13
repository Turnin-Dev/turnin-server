package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import com.peekr.common.util.log.LogLevel
import io.ktor.http.HttpStatusCode

/**
 * 토큰 커스텀 예외
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class TokenException(
    code: ApiErrorCode,
    status: HttpStatusCode = HttpStatusCode.Unauthorized,
    message: String,
    val logLevel: LogLevel,
    cause: Throwable? = null,
) : ApiException(errorCode = code, message = message, status = status, cause = cause) {
    class InvalidTokenException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidToken,
            message = TokenErrorCode.InvalidToken.description,
            logLevel = LogLevel.WARN,
            cause = cause,
        )

    class CannotCreateTokenVerifier(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidVerifier,
            status = HttpStatusCode.InternalServerError,
            message = TokenErrorCode.InvalidVerifier.description,
            logLevel = LogLevel.ERROR,
            cause = cause,
        )

    class CannotCreateToken(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.GenerateTokenError,
            message = TokenErrorCode.GenerateTokenError.description,
            logLevel = LogLevel.ERROR,
            cause = cause,
        )

    class CannotDecodedException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.DecodeTokenError,
            message = TokenErrorCode.DecodeTokenError.description,
            logLevel = LogLevel.WARN,
            cause = cause,
        )

    class UnauthorizedUserException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.UnauthorizedUser,
            status = HttpStatusCode.Forbidden,
            message = TokenErrorCode.UnauthorizedUser.description,
            logLevel = LogLevel.WARN,
            cause = cause,
        )

    class TokenExpiredException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.TokenExpired,
            status = HttpStatusCode.Unauthorized,
            message = TokenErrorCode.TokenExpired.description,
            logLevel = LogLevel.DEBUG,
            cause = cause,
        )

    class VerificationFailedException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.VerificationFailed,
            status = HttpStatusCode.Unauthorized,
            message = TokenErrorCode.VerificationFailed.description,
            logLevel = LogLevel.WARN,
            cause = cause,
        )
}
