package com.turnin.common.model

import com.turnin.common.validator.ValidatorException

class KeywordNameValidationException(message: String) : ValidatorException(message)

@JvmInline
value class KeywordName private constructor(val value: String) {
    /**
     * 키워드 명 VO
     *
     * @throws ValidatorException
     */
    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 15

        operator fun invoke(value: String): KeywordName = KeywordName(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isEmpty()) {
            throw KeywordNameValidationException("키워드 명이 비어있습니다.")
        }
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw KeywordNameValidationException("키워드 길이는 $MIN_LENGTH~$MAX_LENGTH 이내여야 합니다.")
        }
    }
}
