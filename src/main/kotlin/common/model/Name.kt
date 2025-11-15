package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class NameValidationException(message: String) : ValidatorException(message)

@JvmInline
value class Name private constructor(val value: String) {
    /**
     * 사용자 이름 VO
     *
     * @throws ValidatorException
     */
    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 30
        val RegexRule = Regex("^[a-zA-Z0-9가-힣]+$")

        fun from(value: String): Name = Name(value)

        operator fun invoke(value: String): Name = from(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isEmpty()) {
            throw NameValidationException("이름이 비어있습니다.")
        }
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw NameValidationException("이름은 $MIN_LENGTH~$MAX_LENGTH 이내여야 합니다.")
        }
        if (!value.matches(RegexRule)) {
            throw NameValidationException("이름은 영문/숫자/한글만 허용됩니다.")
        }
    }
}
