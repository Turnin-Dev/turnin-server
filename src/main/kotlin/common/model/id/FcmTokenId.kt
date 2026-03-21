package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class FcmTokenIdValidationException(message: String) : ValidatorException(message)

/** FCM 토큰 ID VO */
@JvmInline
value class FcmTokenId private constructor(val value: Long) {
    /**
     * FCM 토큰 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): FcmTokenId = FcmTokenId(value)

        operator fun invoke(value: Long): FcmTokenId = from(value)
    }

    init {
        validate(value)
    }

    private fun validate(value: Long) {
        if (value <= 0) {
            throw FcmTokenIdValidationException("FCM 토큰 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
