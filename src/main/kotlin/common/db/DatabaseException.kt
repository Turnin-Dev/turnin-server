package com.turnin.common.db

import io.ktor.http.HttpStatusCode

/**
 * 데이터베이스 커스텀 예외
 *
 * @property customMessage 커스텀 에러 메시지
 * @property cause [Throwable]
 */
sealed class DatabaseException(
    customMessage: String,
    cause: Throwable,
) : Exception(customMessage, cause) {
    /** DB 쿼리 관련 예외 */
    class DBQueryException(cause: Throwable) : DatabaseException("Database query failed", cause)

    /** 보통 저장 시 중복되는 데이터를 저장할 때 발생하는 예외 */
    class DuplicatedDataException(cause: Throwable) : DatabaseException("Duplicated data detected", cause)

    /** 외래키 제약 위반 예외 */
    class ForeignKeyViolationException(cause: Throwable) : DatabaseException("Foreign key constraint violation", cause)

    /** 제약 조건 위반 예외 */
    class ConstraintViolationException(cause: Throwable) : DatabaseException("Check constraint violation", cause)
}

fun DatabaseException.toHttpStatusCode(): HttpStatusCode = when (this) {
    // 400: 잘못된 요청 (제약 조건 위반. 존재하지 않는 참조 등)
    is DatabaseException.ConstraintViolationException -> HttpStatusCode.BadRequest

    is DatabaseException.ForeignKeyViolationException -> HttpStatusCode.BadRequest

    // 409: 리소스 충돌 (중복 데이터)
    is DatabaseException.DuplicatedDataException -> HttpStatusCode.Conflict

    // 500: 서버 오류 (SQL 구문 오류, 커넥션 실패 등)
    is DatabaseException.DBQueryException -> HttpStatusCode.InternalServerError
}
