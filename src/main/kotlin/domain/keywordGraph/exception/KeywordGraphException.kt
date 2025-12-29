package com.peekr.domain.keywordGraph.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 키워드 그래프 커스텀 예외
 */
sealed class KeywordGraphException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class KeywordIdPairingFailed :
        KeywordGraphException(
            code = KeywordGraphErrorCode.KeywordIdPairingFailed,
            status = HttpStatusCode.InternalServerError,
        )

    class UserNotFound :
        KeywordGraphException(
            code = KeywordGraphErrorCode.UserNotFound,
            status = HttpStatusCode.InternalServerError,
        )
}
