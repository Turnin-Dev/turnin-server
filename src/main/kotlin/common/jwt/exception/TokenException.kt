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
 * @property throwable [Throwable]
 */
sealed class TokenException(
    code: ApiErrorCode,
    status: HttpStatusCode = HttpStatusCode.Unauthorized,
    message: String,
    throwable: Throwable? = null,
) : ApiException(errorCode = code, message = message, status = status, cause = throwable) {
    class InvalidTokenException(throwable: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidToken,
            message = TokenErrorCode.InvalidToken.description,
            throwable = throwable,
        )

    class CannotCreateTokenVerifier(throwable: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.InvalidVerifier,
            message = TokenErrorCode.InvalidVerifier.description,
            throwable = throwable,
        )

    class CannotCreateToken(throwable: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.GenerateTokenError,
            message = TokenErrorCode.GenerateTokenError.description,
            throwable = throwable,
        )

    class CannotDecodedException(throwable: Throwable? = null) :
        TokenException(
            code = TokenErrorCode.DecodeTokenError,
            message = TokenErrorCode.DecodeTokenError.description,
            throwable = throwable,
        )
}
