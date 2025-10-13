package com.peekr.domain.keyword.presentation.validation

import com.peekr.common.validator.PeekrValidator.validation
import com.peekr.domain.keyword.domain.model.Keyword

internal fun String.validateKeywordName() {
    validation(Keyword.isValidKeywordLength(this)) {
        "키워드는 1~15자 이내만 가능합니다."
    }
}
