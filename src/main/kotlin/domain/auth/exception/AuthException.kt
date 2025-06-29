package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * Auth Exception
 *
 * @param detail 에러 설명 (자세히)
 */
sealed class AuthException(
    val detail: String,
    code: ApiErrorCode,
    status: HttpStatusCode,
) : ApiException(code, detail, status) {
    /** 중복된 사용자 예외 (보통 저장할 때 중복된 사용자가 있을 때 예외 처리) */
    class DuplicateUserException(detail: String? = null) :
        AuthException(
            code = AuthErrorCode.UserDuplicated,
            detail = detail ?: AuthErrorCode.UserDuplicated.description,
            status = HttpStatusCode.Conflict,
        )

    class CannotSaveRefreshTokenException(detail: String? = null) :
        AuthException(
            code = AuthErrorCode.CannotSaveRefreshToken,
            detail = detail ?: AuthErrorCode.CannotSaveRefreshToken.description,
            status = HttpStatusCode.Unauthorized,
        )
}
