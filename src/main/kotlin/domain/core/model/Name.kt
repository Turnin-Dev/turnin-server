package com.peekr.domain.core.model

@JvmInline
value class Name private constructor(val value: String) {
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
        require(value.isNotBlank()) {
            "이름이 비어있습니다."
        }
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            "이름은 $MIN_LENGTH~$MAX_LENGTH 이내여야 합니다."
        }
        require(value.matches(RegexRule)) {
            "이름은 영문/숫자/한글만 허용됩니다."
        }
    }
}
