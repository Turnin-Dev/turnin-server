package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class IntroduceValidationException(message: String) : ValidatorException(message)

@JvmInline
value class Introduce private constructor(val value: String) {
    /**
     * (사용자) 소개글 VO
     *
     * @throws IllegalArgumentException
     */
    companion object {
        const val MIN_LENGTH = 0
        const val MAX_LENGTH = 200

        operator fun invoke(value: String): Introduce = Introduce(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw IntroduceValidationException("사용자 소개글 길이는 ${MIN_LENGTH}~${MAX_LENGTH} 이내여야 합니다.")
        }
    }
}
