package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode

sealed class AuthErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object LoginFailed :
        AuthErrorCode(A001, "로그인에 문제가 발생했습니다. (토큰 생성 실패)")
}

private const val A001 = "A001"
