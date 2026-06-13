package com.turnin.common.model.id

import com.turnin.common.validator.ValidatorException

class AnnouncementIdValidationException(message: String) : ValidatorException(message)

/**
 * 공지 ID
 *
 * @throws ValidatorException
 */
@JvmInline
value class AnnouncementId private constructor(val value: Long) {
    companion object {
        operator fun invoke(value: Long) = AnnouncementId(value = value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw AnnouncementIdValidationException("공지 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
