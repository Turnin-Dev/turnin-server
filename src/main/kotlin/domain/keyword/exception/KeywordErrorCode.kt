package com.peekr.domain.keyword.exception

import com.peekr.common.exception.ApiErrorCode

sealed class KeywordErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object UserNotFoundForSave :
        KeywordErrorCode(K001, "키워드를 등록하려는 사용자가 존재하지 않습니다.")
}

private const val K001 = "K001"
