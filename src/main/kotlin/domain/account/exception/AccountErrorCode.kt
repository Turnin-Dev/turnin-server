package com.peekr.domain.account.exception

import com.peekr.common.exception.ApiErrorCode

sealed class AccountErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 사용자를 찾을 수 없는 경우 */
    data object UserNotFound :
        AccountErrorCode(ACC001, "사용자를 찾을 수 없습니다.")
}

private const val ACC001 = "ACC001"
