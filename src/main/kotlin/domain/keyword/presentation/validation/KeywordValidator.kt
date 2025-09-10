package com.peekr.domain.keyword.presentation.validation

import com.peekr.common.validator.PeekrValidator.validation
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.presentation.dto.AddUserKeywordRequest

internal fun AddUserKeywordRequest.validate() {
    UserId.from(this.userId)
    require(this.keyword.length in KEYWORD_MIN_LENGTH..KEYWORD_MAX_LENGTH) {
        "키워드 길이 제한: 1~15자 이내"
    }
}

internal fun String?.validateUserKeywordIdAndReturn(): Long {
    val trimmed = this?.trim()
    validation(trimmed != null) { "사용자 키워드 ID는 비어있을 수 없습니다." }
    validation(trimmed!!.toLongOrNull() != null) { "사용자 키워드 ID는 숫자형식이어야 합니다." }
    return trimmed.toLong()
}

private const val KEYWORD_MIN_LENGTH = 1
private const val KEYWORD_MAX_LENGTH = 15
