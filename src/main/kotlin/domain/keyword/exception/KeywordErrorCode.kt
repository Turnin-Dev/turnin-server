package com.turnin.domain.keyword.exception

import com.turnin.common.exception.ApiErrorCode

sealed class KeywordErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object EmbeddingFailed :
        ApiErrorCode(K001, "키워드 임베딩 과정에서 에러가 발생했습니다.")
}

private const val K001 = "K001"
