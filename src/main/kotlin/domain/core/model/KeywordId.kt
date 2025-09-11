package com.peekr.domain.core.model

/** 키워드 ID VO */
@JvmInline
value class KeywordId private constructor(val value: Long) {
    companion object {
        fun from(value: Long): KeywordId = KeywordId(value)

        operator fun invoke(value: Long): KeywordId = from(value)
    }

    init {
        validateKeywordId()
    }

    private fun validateKeywordId() {
        require(value >= 0) { "키워드 ID는 음수가 될 수 없습니다." }
    }
}
