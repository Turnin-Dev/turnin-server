package com.peekr.domain.account.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * Account Exception
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class AccountException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 사용자를 찾을 수 없는 경우 */
    class UserNotFound(cause: Throwable? = null) :
        AccountException(
            code = AccountErrorCode.UserNotFound,
            status = HttpStatusCode.NotFound,
            cause = cause,
        )
}
