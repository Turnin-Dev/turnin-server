package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class DisplayIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class DisplayId private constructor(val value: String) {
    /**
     * 사용자 표시 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 30
        val RegexRule = Regex("^[a-zA-Z0-9_]+$")

        fun from(value: String): DisplayId = DisplayId(value)

        operator fun invoke(value: String): DisplayId = from(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isEmpty()) {
            throw DisplayIdValidationException("사용자 표시 ID가 비어있습니다.")
        }
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw DisplayIdValidationException("사용자 표시 ID 길이는 $MIN_LENGTH~$MAX_LENGTH 이내여야 합니다.")
        }
        if (!value.matches(RegexRule)) {
            throw DisplayIdValidationException("ID는 영문/숫자/밑줄(_)만 허용됩니다.")
        }
    }
}
