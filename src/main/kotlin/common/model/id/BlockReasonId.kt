package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class BlockReasonIdValidationException(message: String) : ValidatorException(message)

/**
 * 차단 사유 ID VO
 *
 * @see invoke
 */
@JvmInline
value class BlockReasonId private constructor(val value: Long) {
    /**
     * 차단 사유 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        operator fun invoke(value: Long): BlockReasonId = BlockReasonId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw BlockReasonIdValidationException("차단 사유 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
