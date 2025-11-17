package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class UserIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class UserId private constructor(val value: Long) {
    /**
     * 사용자 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): UserId = UserId(value)

        operator fun invoke(value: Long): UserId = from(value)
    }

    init {
        validateUserId(value)
    }

    private fun validateUserId(value: Long) {
        if (value <= 0) {
            throw UserIdValidationException("사용자 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
