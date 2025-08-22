package com.peekr.common.validator

import com.peekr.common.validator.PeekrValidator.validation

object CommonValidator {
    fun validationUserIdAndReturn(userId: Long?): Long {
        validation(userId != null) { "사용자 ID가 필요합니다." }
        validation((userId ?: 0L) > 0) { "사용자 ID는 양수여야 합니다." }
        return userId!!
    }

    fun validationUserIdAndReturn(userId: String?): Long {
        validation(userId != null) { "사용자 ID가 필요합니다." }
        val normalizedUserId = userId!!.trim()
        validation(normalizedUserId.isNotEmpty()) { "사용자 ID가 비어있습니다." }
        validation(normalizedUserId.toLongOrNull() != null) { "사용자 ID는 숫자형식만 허용됩니다." }
        validation((normalizedUserId.toLong()) > 0) { "사용자 ID는 양수여야 합니다." }
        return normalizedUserId.toLong()
    }
}
