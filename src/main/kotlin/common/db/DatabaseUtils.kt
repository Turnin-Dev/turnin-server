package com.peekr.common.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.castTo
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
    ): Column<T> = customEnumeration(
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

    /**
     * enum 타입끼리 비교할 때 사용하는 연산자
     *
     * 내부적으로 문자열 비교를 수행하고, Enum의 name 값을 통해 비교를 수행한다.
     */
    infix fun <T : Enum<T>> Column<T>.eqEnum(value: T): Op<Boolean> =
        this.castTo(VarCharColumnType()) eq value.name
}
