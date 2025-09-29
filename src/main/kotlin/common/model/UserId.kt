package com.peekr.common.model

/** 사용자 ID 래퍼 클래스
 *
 * @throws IllegalArgumentException 사용자 ID 형식에 맞지 않으면 예외 발생
 */
@JvmInline
value class UserId private constructor(val value: Long) {
    companion object {
        fun from(value: Long): UserId = UserId(value)

        operator fun invoke(value: Long): UserId = from(value)
    }

    init {
        validateUserId(value)
    }

    private fun validateUserId(value: Long) {
        require(value >= 0) { "사용자 ID는 음수가 될 수 없습니다." }
    }
}
