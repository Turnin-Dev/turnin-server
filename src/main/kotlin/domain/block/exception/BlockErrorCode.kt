package com.peekr.domain.block.exception

import com.peekr.common.exception.ApiErrorCode

sealed class BlockErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 자기 자신을 차단할 수 없습니다. */
    data object CannotBlockMySelf : BlockErrorCode(B001, "본인을 차단할 수 없습니다.")
}

private const val B001 = "B001"
