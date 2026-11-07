package com.turnin.domain.pingPong.domain.model

import com.turnin.common.validator.ValidatorException

class PingPongQuestionContentValidationException(message: String) : ValidatorException(message)

@JvmInline
value class PingPongQuestionContent private constructor(val value: String) {
    /**
     * 핑퐁 질문 내용 VO
     *
     * 답변 내용([PingPongAnswerContent])과 글자 수 규칙은 같지만, 질문 형식 검증이 추가될 수 있어 별도 VO로 둔다.
     *
     * @throws ValidatorException
     */
    companion object {
        const val MAX_LENGTH = 2200

        operator fun invoke(value: String): PingPongQuestionContent = PingPongQuestionContent(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw PingPongQuestionContentValidationException("질문 내용이 비어있습니다.")
        }
        if (value.length > MAX_LENGTH) {
            throw PingPongQuestionContentValidationException("질문 내용은 ${MAX_LENGTH}자 이내여야 합니다.")
        }
    }
}
