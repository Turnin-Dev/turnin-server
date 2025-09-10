package com.peekr.domain.keyword.domain.model

/** 키워드 ID 래퍼 클래스 */
@JvmInline
value class KeywordId private constructor(val value: Long) {
    companion object {
        fun from(value: Long): KeywordId = KeywordId(value)

        operator fun invoke(value: Long): KeywordId = KeywordId(value)
    }

    init {
        validateKeywordId()
    }

    private fun validateKeywordId() {
        require(value >= 0) { "키워드 ID는 음수가 될 수 없습니다." }
    }
}
