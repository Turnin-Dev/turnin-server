package com.turnin.domain.pingPong.domain.model

import com.turnin.common.validator.ValidatorException

class PingPongContentValidationException(message: String) : ValidatorException(message)

@JvmInline
value class PingPongContent private constructor(val value: String) {
    /**
     * 핑퐁 내용(질문/답변) VO
     *
     * @throws ValidatorException
     */
    companion object {
        const val MAX_LENGTH = 2200

        operator fun invoke(value: String): PingPongContent = PingPongContent(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw PingPongContentValidationException("핑퐁 내용이 비어있습니다.")
        }
        if (value.length > MAX_LENGTH) {
            throw PingPongContentValidationException("핑퐁 내용은 ${MAX_LENGTH}자 이내여야 합니다.")
        }
    }
}
