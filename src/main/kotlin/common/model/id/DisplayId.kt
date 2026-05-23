package com.turnin.common.model.id

import com.turnin.common.validator.ValidatorException

class DisplayIdValidationException(message: String) : ValidatorException(message)

/**
 * 사용자 표시 ID VO
 */
@JvmInline
value class DisplayId private constructor(val value: String) {
    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 30

        /** 사용자 표시 ID 규칙: 영어/숫자/밑줄/점 허용, 시작·끝은 영어/숫자만 */
        val RegexRule = Regex("^[a-zA-Z0-9][a-zA-Z0-9._]*[a-zA-Z0-9]$|^[a-zA-Z0-9]$")

        fun from(value: String): DisplayId = DisplayId(value)

        operator fun invoke(value: String): DisplayId = from(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw DisplayIdValidationException("사용자 표시 ID가 비어있습니다.")
        }
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw DisplayIdValidationException("사용자 표시 ID 길이는 ${MIN_LENGTH}~${MAX_LENGTH} 이내여야 합니다.")
        }
        if (!value.matches(RegexRule)) {
            throw DisplayIdValidationException("ID는 영어/숫자/특수문자(_ .)만 허용되며, 시작·끝은 영어/숫자여야 합니다.")
        }
    }
}
