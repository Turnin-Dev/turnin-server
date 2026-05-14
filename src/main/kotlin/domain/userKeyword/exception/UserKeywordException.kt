package com.turnin.domain.userKeyword.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 사용자 키워드 커스텀 예외
 */
sealed class UserKeywordException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 존재하지 않은 키워드 조회 예외 */
    class NotExistsKeyword(cause: Throwable?) :
        UserKeywordException(
            code = UserKeywordErrorCode.NotExistsKeyword,
            status = HttpStatusCode.NotFound,
            cause = cause,
        )

    class CountLimitReached :
        UserKeywordException(
            code = UserKeywordErrorCode.CountLimitReached,
            status = HttpStatusCode.BadRequest,
        )

    class UpdateFailed :
        UserKeywordException(
            code = UserKeywordErrorCode.UpdateFailed,
            status = HttpStatusCode.NotFound,
        )
}
