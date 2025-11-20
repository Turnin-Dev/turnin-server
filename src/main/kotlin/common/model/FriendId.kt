package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class FriendIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class FriendId private constructor(val value: Long) {
    companion object {
        operator fun invoke(value: Long): FriendId = FriendId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw FriendIdValidationException("친구 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
