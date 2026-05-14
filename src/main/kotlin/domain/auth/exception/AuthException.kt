package com.turnin.domain.auth.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * Auth Exception
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class AuthException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 중복된 사용자 예외 (보통 저장할 때 중복된 사용자가 있을 때 예외 처리) */
    class DuplicateUserException(cause: Throwable? = null) :
        AuthException(
            code = AuthErrorCode.UserDuplicated,
            status = HttpStatusCode.Conflict,
            cause = cause,
        )

    class UserNotFound(cause: Throwable? = null) :
        AuthException(
            code = AuthErrorCode.UserNotFound,
            status = HttpStatusCode.NotFound,
            cause = cause,
        )

    class RefreshTokenSaveFailed(cause: Throwable? = null) :
        AuthException(
            code = AuthErrorCode.RefreshTokenSaveFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )
}
