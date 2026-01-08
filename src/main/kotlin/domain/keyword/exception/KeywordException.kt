package com.peekr.domain.keyword.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class KeywordException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class EmbeddingFailed(cause: Throwable?) :
        KeywordException(
            code = KeywordErrorCode.EmbeddingFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )
}
