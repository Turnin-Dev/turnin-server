package com.turnin.common.model.id

import com.turnin.common.validator.ValidatorException

class PingPongAnswerIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class PingPongAnswerId private constructor(val value: Long) {
    /**
     * 핑퐁 답변 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        operator fun invoke(value: Long): PingPongAnswerId = PingPongAnswerId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw PingPongAnswerIdValidationException("핑퐁 답변 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
