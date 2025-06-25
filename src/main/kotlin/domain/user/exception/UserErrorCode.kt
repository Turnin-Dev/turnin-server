package com.peekr.domain.user.exception

import com.peekr.common.exception.ApiErrorCode

sealed class UserErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object UserNotFound : UserErrorCode(NF001, "사용자를 찾을 수 없습니다.")
}

private const val NF001 = "NF001"
