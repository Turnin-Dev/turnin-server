package com.peekr.domain.core.model

@JvmInline
value class DisplayId private constructor(val value: String) {
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
        require(value.isNotBlank()) {
            "사용자 표시 ID가 비어있습니다."
        }
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            "사용자 표시 ID 길이는 $MIN_LENGTH~$MAX_LENGTH 이내여야 합니다."
        }
        require(value.matches(RegexRule)) {
            "ID는 영문/숫자/밑줄(_)만 허용됩니다."
        }
    }
}
