package com.peekr.domain.core.model

/** 사용자 ID 래퍼 클래스 */
data class UserId(val id: Long) {
    companion object {
        /**
         * @throws IllegalArgumentException 사용자 ID 형식에 맞지 않으면 예외 발생
         */
        fun from(value: String?): UserId {
            require(value != null) { "사용자 ID가 비어있습니다." }
            require(value.isNotEmpty()) { "사용자 ID가 비어있습니다." }
            require(value.toLongOrNull() != null) { "사용자 ID는 숫자형식만 허용됩니다." }
            require(value.toLong() >= 0) { "사용자 ID는 음수가 될 수 없습니다." }
            return UserId(value.toLong())
        }

        /**
         * @throws IllegalArgumentException 사용자 ID 형식에 맞지 않으면 예외 발생
         */
        fun from(value: Long?): UserId {
            require(value != null) { "사용자 ID가 비어있습니다." }
            require(value >= 0) { "사용자 ID는 음수가 될 수 없습니다." }
            return UserId(value)
        }
    }
}
