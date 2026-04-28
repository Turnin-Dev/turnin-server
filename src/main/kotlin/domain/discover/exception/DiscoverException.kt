package com.turnin.domain.discover.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 키워드 그래프 커스텀 예외
 */
sealed class DiscoverException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class KeywordIdPairingFailed :
        DiscoverException(
            code = DiscoverErrorCode.KeywordIdPairingFailed,
            status = HttpStatusCode.InternalServerError,
        )

    class UserNotFound :
        DiscoverException(
            code = DiscoverErrorCode.UserNotFound,
            status = HttpStatusCode.InternalServerError,
        )
}
