package com.peekr.domain.keyword.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * Keyword Exception
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class KeywordException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 키워드를 저장하려는 사용자를 찾지 못할 때 */
    class UserNotFoundForSave :
        KeywordException(
            code = KeywordErrorCode.UserNotFoundForSave,
            status = HttpStatusCode.NotFound,
        )
}
