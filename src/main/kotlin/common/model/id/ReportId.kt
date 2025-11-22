package com.peekr.common.model.id

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
        operator fun invoke(value: Long): ReportId = ReportId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw ReportIdValidationException("신고 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
