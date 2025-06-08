package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class TokenException(
    code: ApiErrorCode,
    message: String,
    status: HttpStatusCode = HttpStatusCode.Unauthorized,
) : ApiException(errorCode = code, message = message, status = status) {
    class InvalidTokenException :
        TokenException(
            code = TokenErrorCode.InvalidToken,
            message = TokenErrorCode.InvalidToken.description,
        )

    class CannotCreateTokenVerifier :
        TokenException(
            code = TokenErrorCode.InvalidVerifier,
            message = TokenErrorCode.InvalidVerifier.description,
        )
}
