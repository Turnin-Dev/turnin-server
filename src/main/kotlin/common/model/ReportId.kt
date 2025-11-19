package com.peekr.common.model

import com.peekr.common.validator.ValidatorException

class ReportIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class ReportId private constructor(val value: Long) {
    /**
     * 신고 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        val UNSAVED = ReportId(0)

        operator fun invoke(value: Long): ReportId = ReportId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value < 0) {
            throw ReportIdValidationException("신고 ID는 음수가 될 수 없습니다.")
        }
    }
}
