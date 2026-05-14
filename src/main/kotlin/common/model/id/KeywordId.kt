package com.turnin.common.model.id

import com.turnin.common.validator.ValidatorException

class KeywordIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class KeywordId private constructor(val value: Long) {
    /**
     * 키워드 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): KeywordId = KeywordId(value)

        operator fun invoke(value: Long): KeywordId = from(value)
    }

    init {
        validateKeywordId()
    }

    private fun validateKeywordId() {
        if (value <= 0) {
            throw KeywordIdValidationException("키워드 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
