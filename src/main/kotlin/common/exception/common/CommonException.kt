package com.turnin.common.exception.common

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 공통 예외
 */
sealed class CommonException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class AccessDenied :
        CommonException(
            code = CommonErrorCode.AccessDenied,
            status = HttpStatusCode.Forbidden,
        )
}
