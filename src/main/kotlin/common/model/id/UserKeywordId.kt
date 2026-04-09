package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class UserKeywordIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class UserKeywordId private constructor(val value: Long) {
    /**
     * 사용자 키워드 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): UserKeywordId {
            if (value <= 0) throw UserKeywordIdValidationException("사용자 키워드 ID는 0이나 음수가 될 수 없습니다.")
            return UserKeywordId(value)
        }

        operator fun invoke(value: Long): UserKeywordId = from(value)
    }
}
