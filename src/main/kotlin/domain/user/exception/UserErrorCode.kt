package com.peekr.domain.user.exception

import com.peekr.common.exception.ApiErrorCode

sealed class UserErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object UserNotFound : UserErrorCode(U001, "사용자를 찾을 수 없습니다.")

    data object UserPatchFailed : UserErrorCode(U002, "사용자 정보가 수정되지 않았습니다.")

    data object IntroducePatchFailed : UserErrorCode(U003, "소개글이 수정되지 않았습니다.")
}

private const val U001 = "U001"
private const val U002 = "U002"
private const val U003 = "U003"
