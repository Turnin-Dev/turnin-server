package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class ReportReasonIdValidationException(message: String) : ValidatorException(message)

@JvmInline
value class ReportReasonId private constructor(val value: Long) {
    /**
     * 신고 사유 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        operator fun invoke(value: Long): ReportReasonId = ReportReasonId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw ReportReasonIdValidationException("신고 사유 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
