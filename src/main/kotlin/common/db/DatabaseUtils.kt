package com.peekr.common.db

import com.peekr.common.db.schema.Blocks
import java.time.OffsetDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.javatime.JavaOffsetDateTimeColumnType
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.notExists
import org.jetbrains.exposed.sql.or
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
     * [myUserId]와 [otherUserIdColumn] 간에 차단 관계가 존재하는지 확인한다.
     * 차단 관계가 없다면 `true`를 반환하여 해당 행이 결과에 포함되게 하고,
     * 차단 관계(양방향 중 하나라도)가 있다면 `false`를 반환하여 결과에서 제외한다.
     *
     * 나 자신인 경우([myUserId]와 [otherUserIdColumn]가 같은 경우)에는 항상 `True`를 반환한다.
     *
     * @param myUserId 나의 사용자 ID
     * @param otherUserIdColumn 다른 사용자 ID 컬럼
     */
    fun SqlExpressionBuilder.isNotBlockedRelationship(
        myUserId: Long,
        otherUserIdColumn: Column<EntityID<Long>>,
    ): Op<Boolean> {
        // 나 자신인 경우에는 차단 여부를 확인할 필요 없이 항상 True를 반환
        val isMyId = otherUserIdColumn eq myUserId

        return isMyId or notExists(
            Blocks
                .select(intLiteral(1))
                .where {
                    (Blocks.blockerId eq myUserId and (Blocks.blockedId eq otherUserIdColumn)) or
                        (Blocks.blockerId eq otherUserIdColumn and (Blocks.blockedId eq myUserId))
                },
        )
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
