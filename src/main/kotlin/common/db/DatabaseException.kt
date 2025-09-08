package com.peekr.common.db

/**
 * 데이터베이스 커스텀 예외
 *
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class DatabaseException(
    message: String,
    cause: Throwable,
) : Exception(message, cause) {
    /** DB 쿼리 관련 예외 */
    class DBQueryException(cause: Throwable) : DatabaseException("Database query failed", cause)

    /** 보통 저장 시 중복되는 데이터를 저장할 때 발생하는 예외 */
    class DuplicatedDataException(cause: Throwable) : DatabaseException("Duplicated data detected", cause)

    /** 외래키 제약 위반 예외 */
    class ForeignKeyViolationException(cause: Throwable) : DatabaseException("Foreign key constraint violation", cause)
}
