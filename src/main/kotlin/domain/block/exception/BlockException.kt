package com.peekr.domain.block.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class BlockException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 자기 자신을 차단하는 경우 발생하는 예외 */
    class CannotBlockMySelf(cause: Throwable? = null) :
        BlockException(
            code = BlockErrorCode.CannotBlockMySelf,
            status = HttpStatusCode.BadRequest,
            cause = cause,
        )
}
