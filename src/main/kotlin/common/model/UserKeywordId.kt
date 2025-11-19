package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class UserKeywordIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class UserKeywordId(val value: Long) {
    /**
     * 사용자 키워드 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): UserKeywordId = UserKeywordId(value)

        operator fun invoke(value: Long): UserKeywordId = from(value)
    }

    init {
        validateUserKeywordId()
    }

    fun validateUserKeywordId() {
        if (value < 0) {
            throw UserKeywordIdValidationException("사용자 키워드 ID는 음수가 될 수 없습니다.")
        }
    }
}
