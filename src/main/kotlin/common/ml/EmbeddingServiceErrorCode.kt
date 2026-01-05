package com.peekr.common.ml

import com.peekr.common.exception.ApiErrorCode

sealed class EmbeddingServiceErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object InferenceError :
        EmbeddingServiceErrorCode(EMB001, "키워드를 분석하는 과정에서 에러가 발생했습니다.")

    data object TokenizationFailed :
        EmbeddingServiceErrorCode(EMB002, "키워드를 토큰화하는 과정에서 실패했습니다.")
}

private const val EMB001 = "EMB001"
private const val EMB002 = "EMB002"
