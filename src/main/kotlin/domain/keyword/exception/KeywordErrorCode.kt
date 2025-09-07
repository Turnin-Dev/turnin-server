package com.peekr.domain.keyword.exception

import com.peekr.common.exception.ApiErrorCode

sealed class KeywordErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object KeywordDuplicated :
        KeywordErrorCode(K001, "서비스 전체에서 키워드는 중복될 수 없습니다.")
}

private const val K001 = "K001"
