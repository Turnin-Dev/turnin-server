package com.peekr.domain.keyword.presentation.validation

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.presentation.dto.AddUserKeywordRequest

internal fun AddUserKeywordRequest.validate() {
    UserId(this.userId)
    require(this.keyword.length in KEYWORD_MIN_LENGTH..KEYWORD_MAX_LENGTH) {
        "키워드 길이 제한: 1~15자 이내"
    }
}

private const val KEYWORD_MIN_LENGTH = 1
private const val KEYWORD_MAX_LENGTH = 15
