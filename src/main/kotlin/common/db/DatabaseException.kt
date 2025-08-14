package com.peekr.common.db

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 데이터베이스 커스텀 예외
 *
 * @property detail 예외 자세한 설명
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 */
sealed class DatabaseException(
    val detail: String,
    code: ApiErrorCode,
    status: HttpStatusCode,
) : ApiException(code, detail, status) {
    class DBQueryException(detail: String? = null) :
        DatabaseException(
            code = DatabaseErrorCode.DBQueryError,
            detail = detail ?: DatabaseErrorCode.DBQueryError.description,
            status = HttpStatusCode.InternalServerError,
        )
}
