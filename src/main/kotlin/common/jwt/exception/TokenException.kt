package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class TokenException(
    val detail: String,
    code: ApiErrorCode,
    status: HttpStatusCode = HttpStatusCode.Unauthorized,
) : ApiException(errorCode = code, message = detail, status = status) {
    class InvalidTokenException(detail: String? = null) :
        TokenException(
            code = TokenErrorCode.InvalidToken,
            detail = detail ?: TokenErrorCode.InvalidToken.description,
        )

    class CannotCreateTokenVerifier(detail: String? = null) :
        TokenException(
            code = TokenErrorCode.InvalidVerifier,
            detail = detail ?: TokenErrorCode.InvalidVerifier.description,
        )

    class CannotCreateToken(detail: String? = null) :
        TokenException(
            code = TokenErrorCode.GenerateTokenError,
            detail = detail ?: TokenErrorCode.GenerateTokenError.description,
        )
}
