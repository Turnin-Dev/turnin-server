package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * Auth Exception
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property throwable [Throwable]
 */
sealed class AuthException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    throwable: Throwable? = null,
) : ApiException(code, status, message, throwable) {
    /** 중복된 사용자 예외 (보통 저장할 때 중복된 사용자가 있을 때 예외 처리) */
    class DuplicateUserException(throwable: Throwable? = null) :
        AuthException(
            code = AuthErrorCode.UserDuplicated,
            status = HttpStatusCode.Conflict,
            throwable = throwable,
        )

    class CannotSaveRefreshTokenException(throwable: Throwable? = null) :
        AuthException(
            code = AuthErrorCode.CannotSaveRefreshToken,
            status = HttpStatusCode.Conflict,
            throwable = throwable,
        )
}
