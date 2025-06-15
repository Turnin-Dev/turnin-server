package com.peekr.common.db

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

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
