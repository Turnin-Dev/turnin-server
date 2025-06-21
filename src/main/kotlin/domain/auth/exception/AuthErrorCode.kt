package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode

sealed class AuthErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 로그인 실패 에러 */
    data object LoginFailed :
        AuthErrorCode(A001, "로그인에 문제가 발생했습니다. (토큰 생성 실패)")

    /** 중복된 사용자 에러 */
    data object UserDuplicated :
        AuthErrorCode(A002, "중복된 사용자 입니다.")
}

private const val A001 = "A001"
private const val A002 = "A002"
