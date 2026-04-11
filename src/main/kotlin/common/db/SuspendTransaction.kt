package com.peekr.common.db

import com.peekr.common.util.AppDispatchers.ioDispatcher
import com.peekr.common.util.log.AppLoggerFactory
import java.sql.SQLException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * DB 쿼리 작업을 수행할 때 항상 이 범위 내에서 수행한다.
 *
 * 이 헬퍼 함수는 중첩 트랜잭션 문제를 방지한다.
 *
 * **유의할 점:**
 * 1. `newSuspendedTransaction { ... }`은 Exposed가 제공하는 함수 자체에 예외 발생 시 자동으로 트랜잭션 롤백을 수행하는 로직이 내장되어 있기 때문에
 * else 블록 부분은 안전하나 if 블록(block() 호출 부분)은 기존 트랜잭션 컨텍스트 내에서 실행되며, 예외 발생 시 Exposed에게 명시적인 롤백 명령을 내린다는 보장이 없다.
 * 하지만 기본적으로 Exposed가 최상위 트랜잭션 블록이 예외를 받으면 자동 롤백을 시도하기 때문에 최대한 예외를 최상위 트랜잭션 블록 (Ex. 유스케이스)까지
 * 전파되도록 하는 것을 권장한다.
 * 2. 해당 트랜잭션은 DB 작업을 수행하기 위해 존재하기 때문에 반드시 `IO 디스패처`에서 실행되어야 한다.
 *
 * @throws DatabaseException.DBQueryException 기타 DB 쿼리 오류 시
 * @throws DatabaseException.DuplicatedDataException 중복 데이터 저장 시도 시
 * @throws DatabaseException.ForeignKeyViolationException 외래키 제약조건 위반 시
 */
suspend fun <T> suspendTransaction(block: suspend Transaction.() -> T): T =
    try {
        if (TransactionManager.currentOrNull() != null) {
            block(TransactionManager.current())
        } else {
            newSuspendedTransaction(ioDispatcher) {
                block()
            }
        }
    } catch (e: ExposedSQLException) {
        LOGGER.error("Database query failed: ${e.cause?.message}")
        throw handleSqlException(e)
    } catch (e: SQLException) {
        LOGGER.error("Database query failed: ${e.cause?.message}")
        throw handleSqlException(e)
    }

/**
 * SQL 예외를 비즈니스 예외로 변환하는 함수
 *
 * - 중복 예외: [DatabaseException.DuplicatedDataException]
 * - 외래키 제약조건 위반: [DatabaseException.ForeignKeyViolationException]
 * - 그 외의 경우: [DatabaseException.DBQueryException]
 *
 * @param e 변환할 SQL 예외
 */
private fun handleSqlException(e: Throwable): DatabaseException {
    val sqlState: String? = when (e) {
        is ExposedSQLException -> e.sqlState
        is SQLException -> e.sqlState
        else -> null
    }

    val message = e.cause?.message?.lowercase() ?: e.message?.lowercase() ?: ""

    // 중복 데이터 검사 (PostgreSQL 기준)
    val isDuplicate = sqlState == "23505" ||
        sequenceOf(message).any { msg ->
            "already exists" in msg ||
                "duplicate key" in msg ||
                "unique constraint" in msg ||
                "primary key violation" in msg
        }

    // 외래키 제약조건 위반 검사 (PostgreSQL 기준)
    val isForeignKeyViolation = sqlState == "23503" ||
        sequenceOf(message).any { msg ->
            "foreign key constraint" in msg ||
                "referential integrity" in msg ||
                "cannot add or update a child row" in msg ||
                "violates foreign key constraint" in msg
        }

    // 제약조건 위반 검사 (PostgreSQL 기준)
    val isConstraintViolation = sqlState == "23514" ||
        sequenceOf(message).any { msg ->
            "check constraint violation" in msg
        }

    return when {
        isDuplicate -> {
            LOGGER.debug("Duplicate data detected while saving data.", e)
            DatabaseException.DuplicatedDataException(e)
        }

        isForeignKeyViolation -> {
            LOGGER.debug("Foreign key constraint violation detected.", e)
            DatabaseException.ForeignKeyViolationException(e)
        }

        isConstraintViolation -> {
            LOGGER.debug("Check constraint violation detected.", e)
            DatabaseException.ConstraintViolationException(e)
        }

        else -> {
            LOGGER.warn("Unexpected database exception", e)
            DatabaseException.DBQueryException(e)
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("SuspendTransaction")
