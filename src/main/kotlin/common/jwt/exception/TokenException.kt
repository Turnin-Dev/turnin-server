package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
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
    cause: Throwable? = null,
) : ApiException(errorCode = code, message = message, status = status, cause = cause) {
    class InvalidTokenException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidToken,
            message = TokenErrorCode.InvalidToken.description,
            cause = cause,
        )

    class CannotCreateTokenVerifier(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidVerifier,
            status = HttpStatusCode.InternalServerError,
            message = TokenErrorCode.InvalidVerifier.description,
            cause = cause,
        )

    class CannotCreateToken(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.GenerateTokenError,
            message = TokenErrorCode.GenerateTokenError.description,
            cause = cause,
        )

    class CannotDecodedException(cause: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.DecodeTokenError,
            message = TokenErrorCode.DecodeTokenError.description,
            cause = cause,
        )
}
