package com.peekr.common.model.id

import com.peekr.common.validator.ValidatorException

class BlockIdValidationException(message: String) : ValidatorException(message)

/**
 * 차단 ID VO
 *
 * @see invoke
 */
@JvmInline
value class BlockId private constructor(val value: Long) {
    /**
     * 차단 ID VO
     *
     * @throws ValidatorException
     */
    companion object {
        operator fun invoke(value: Long): BlockId = BlockId(value)
    }

    init {
        validate()
    }

    private fun validate() {
        if (value <= 0) {
            throw BlockIdValidationException("차단 ID는 0이나 음수가 될 수 없습니다.")
        }
    }
}
