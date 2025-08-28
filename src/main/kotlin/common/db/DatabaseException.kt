package com.peekr.common.db

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode
import java.sql.SQLException

/**
 * 데이터베이스 커스텀 예외
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property throwable [Throwable]
 */
sealed class DatabaseException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    throwable: Throwable? = null,
) : ApiException(code, status, message, throwable) {
    /**
     * DB 쿼리 관련 예외
     */
    class DBQueryException(val throwable: SQLException? = null) :
        DatabaseException(
            code = DatabaseErrorCode.DBQueryError,
            status = HttpStatusCode.InternalServerError,
            throwable = throwable,
        )
}
