package com.peekr.domain.keyword.presentation.validation

import com.peekr.common.validator.PeekrValidator.validation

internal fun String?.validateUserKeywordIdAndReturn(): Long {
    validation(this != null) { "사용자 키워드는 비어있을 수 없습니다." }
    validation(this!!.toLongOrNull() != null) { "사용자 키워드는 숫자형식이어야 합니다." }
    return this.toLong()
}
