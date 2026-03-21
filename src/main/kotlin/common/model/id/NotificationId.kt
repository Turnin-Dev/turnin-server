package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class NotificationIdValidationException(message: String) : ValidatorException(message)

/** 알림 ID VO */
@JvmInline
value class NotificationId private constructor(val value: Long) {
    /**
     * 알림 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        fun from(value: Long): NotificationId = NotificationId(value)

        operator fun invoke(value: Long): NotificationId = from(value)
    }

    init {
        validate(value)
    }

    private fun validate(value: Long) {
        if (value <= 0) {
            throw NotificationIdValidationException("알림 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
