package com.turnin.common.model.id

import com.turnin.common.validator.ValidatorException

class PingPongIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class PingPongId private constructor(val value: Long) {
    /**
     * 핑퐁 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        operator fun invoke(value: Long): PingPongId = PingPongId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw PingPongIdValidationException("핑퐁 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
