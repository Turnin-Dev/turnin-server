package com.peekr.domain.core.model

/** 사용자별 키워드 ID 래퍼 클래스 */
@JvmInline
value class UserKeywordId(val value: Long) {
    companion object {
        fun from(value: Long): UserKeywordId = UserKeywordId(value)

        operator fun invoke(value: Long): UserKeywordId = UserKeywordId(value)
    }

    init {
        validateUserKeywordId()
    }

    fun validateUserKeywordId() {
        require(value >= 0) { "사용자 키워드 ID는 음수가 될 수 없습니다." }
    }
}
