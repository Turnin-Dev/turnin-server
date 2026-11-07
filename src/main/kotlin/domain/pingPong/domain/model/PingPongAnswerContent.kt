package com.turnin.domain.pingPong.domain.model

import com.turnin.common.validator.ValidatorException

class PingPongAnswerContentValidationException(message: String) : ValidatorException(message)

@JvmInline
value class PingPongAnswerContent private constructor(val value: String) {
    /**
     * 핑퐁 답변 내용 VO
     *
     * @throws ValidatorException
     */
    companion object {
        const val MAX_LENGTH = 2200

        operator fun invoke(value: String): PingPongAnswerContent = PingPongAnswerContent(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw PingPongAnswerContentValidationException("답변 내용이 비어있습니다.")
        }
        if (value.length > MAX_LENGTH) {
            throw PingPongAnswerContentValidationException("답변 내용은 ${MAX_LENGTH}자 이내여야 합니다.")
        }
    }
}
