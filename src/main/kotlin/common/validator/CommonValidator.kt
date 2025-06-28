package com.peekr.common.validator

import com.peekr.common.validator.PeekrValidator.validation

object CommonValidator {
    fun userIdValidatorAndReturn(userId: String?): Long {
        validation(userId != null) { "사용자 ID가 필요합니다." }
        userId?.let {
            validation(userId.isNotEmpty()) { "사용자 ID가 비어있습니다." }
            validation(userId.toLongOrNull() != null) { "사용자 ID는 숫자형식만 허용됩니다." }
        }
        return userId!!.toLong()
    }
}
