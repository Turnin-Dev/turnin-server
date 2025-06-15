package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class AuthException(
    val detail: String,
    code: ApiErrorCode,
    status: HttpStatusCode,
) : ApiException(code, detail, status) {
    class DuplicateUserException(detail: String? = null) :
        AuthException(
            code = AuthErrorCode.UserDuplicated,
            detail = detail ?: AuthErrorCode.UserDuplicated.description,
            status = HttpStatusCode.Conflict,
        )
}
