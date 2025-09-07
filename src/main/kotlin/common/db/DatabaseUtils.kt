package com.peekr.common.db

import java.sql.SQLException
import java.time.OffsetDateTime
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.javatime.JavaOffsetDateTimeColumnType
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject

/**
 * 데이터베이스 관련 (스키마, 테이블 등) 유틸
 */
object DatabaseUtils {
    /**
     * enum 형식의 커스텀 컬럼이 필요할 때 사용
     *
     * - `fromDb`: 데이터베이스 -> kotlin enum 변환 함수 (DB에서 읽어온 문자열 값을 Kotlin enum으로 변환)
     * - `toDb`: kotlin enum -> 데이터베이스 변환 함수 (Kotlin enum을 DB에 저장할 값으로 변환)
     *
     * @param name 테이블 컬럼명
     * @param sqlName 데이터베이스에서 사용할 SQL 타입 이름 (null이면 기본 타입 사용)
     */
    inline fun <reified T : Enum<T>> Table.customPostgresEnum(
        name: String,
        sqlName: String?,
    ): Column<T> {
        val isPostgres = currentDialect is PostgreSQLDialect

        return if (isPostgres) {
            customEnumeration(
                name = name,
                sql = sqlName,
                fromDb = { value ->
                    when (value) {
                        is String -> enumValueOf<T>(value)
                        is PGobject -> enumValueOf<T>(value.value!!)
                        else -> enumValueOf<T>(value.toString())
                    }
                },
                toDb = { enum ->
                    PGobject().apply {
                        type = sqlName
                        value = enum.name
                    }
                },
            )
        } else {
            enumerationByName(name, 50, T::class)
        }
    }

    /**
     * enum 타입끼리 비교할 때 사용하는 연산자
     *
     * 내부적으로 문자열 비교를 수행하고, Enum의 name 값을 통해 비교를 수행한다.
     */
    infix fun <T : Enum<T>> Column<T>.eqEnum(value: T): Op<Boolean> =
        this.castTo(VarCharColumnType()) eq value.name

    /**
     * [timestampWithTimeZone] 간소화 버전
     *
     * `Postgres`일 때만 `timestampWithTimeZone`사용하고 이 외에는 대체 타입 사용
     */
    fun Table.timestamptz(name: String): Column<OffsetDateTime> = if (currentDialect is PostgreSQLDialect) {
        timestampWithTimeZone(name).defaultExpression(timestampExpression)
    } else {
        registerColumn(name, JavaOffsetDateTimeColumnType()).defaultExpression(timestampExpression)
    }

    /**
     * 저장 시 중복되는 값이 있을 때 [onExists] 람다에서 커스텀 예외를 던져 발생시키거나 [e]예외를 그대로 전파한다.
     *
     * @param e 잡을 예외
     * @param onExists 중복되는 값이 있을 때 커스텀 예외 발생
     */
    inline fun processExceptionForSave(
        e: Exception,
        onExists: () -> Throwable,
    ): Throwable {
        val sqlState: String? = when (e) {
            is ExposedSQLException -> e.sqlState
            is SQLException -> e.sqlState
            is DatabaseException.DBQueryException -> e.throwable?.sqlState
            else -> null
        }
        val causeMsg = when (e) {
            is DatabaseException.DBQueryException -> e.throwable?.message
            is ExposedSQLException -> e.cause?.message
            else -> null
        }
        val isDuplicateByMsg = sequenceOf(e.message, causeMsg).any {
            val msg = it?.lowercase() ?: return@any false
            "already exists" in msg ||
                "duplicate key" in msg ||
                "unique constraint" in msg ||
                "primary key violation" in msg
        }
        return if (sqlState == "23505" || isDuplicateByMsg) {
            onExists()
        } else {
            e
        }
    }
}

/**
 * Exposed Expression<OffsetDateTime> 타입의 타임스탬프
 *
 * `CURRENT_TIMESTAMP`를 그대로 사용하므로, 반환되는 시점의 타임존은 DB 세션의 TimeZone 설정(예: 서버 기본 시간대) 영향을 받는다.
 *
 * (→ DB 세션이 반드시 UTC로 작동하는지, 혹은 CURRENT_TIMESTAMP AT TIME ZONE 'UTC' 등 추가 보정이 필요한지 확인해야 한다.)
 */
private val timestampExpression: Expression<OffsetDateTime> = object : Function<OffsetDateTime>(
    JavaOffsetDateTimeColumnType(),
) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { append("CURRENT_TIMESTAMP") }
}
