package com.turnin.common.model

import com.turnin.common.validator.ValidatorException

class NameValidationException(message: String) : ValidatorException(message)

/**
 * 사용자 이름 VO
 */
@JvmInline
value class UserName private constructor(val value: String) {
    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 30

        /** 사용자 이름 규칙: 영어/숫자/한글/특수문자(. - ' _ ! ?) 허용, 공백 허용하되 시작·끝 불가 */
        val RegexRule = Regex("^[a-zA-Z0-9가-힣 .\\-'_!?]+$")

        fun from(value: String): UserName = UserName(value)

        operator fun invoke(value: String): UserName = from(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw NameValidationException("이름이 비어있습니다.")
        }
        if (value.first().isWhitespace() || value.last().isWhitespace()) {
            throw NameValidationException("이름은 공백으로 시작하거나 끝날 수 없습니다.")
        }
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw NameValidationException("이름은 ${MIN_LENGTH}~${MAX_LENGTH}자 이내여야 합니다.")
        }
        if (!value.matches(RegexRule)) {
            throw NameValidationException("이름에 사용할 수 없는 문자가 포함되어 있습니다.")
        }
    }
}
