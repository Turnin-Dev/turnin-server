package com.turnin.common.db

import com.turnin.common.db.DatabaseUtils.timestamptz
import com.turnin.common.util.PeekrDateTime
import com.turnin.common.util.toOffsetDateTime
import io.ktor.util.logging.KtorSimpleLogger
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.toEntity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.UpsertStatement
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

// ------------------------------ BaseLongIdTable ------------------------------

/**
 * 모든 `LongIdTable`의 기초가 되는 추상 클래스
 *
 * Exposed의 DAO 스타일의 테이블 명세(복수형으로 정의된 테이블)에서 사용
 *
 * ##### 사용 예시
 * ```
 * object Users : BaseLongIdTable("user") {
 *     val name = varchar("name", 50)
 *     val profileImageUrl = varchar("profile_image_url", 500).nullable()
 *     val introduce = text("introduce").nullable()
 * }
 * ```
 */
abstract class BaseLongIdTable(
    name: String,
    idName: String = "id",
) : LongIdTable(name, idName) {
    val createdAt = timestamptz("created_at")
    val updatedAt = timestamptz("updated_at")
}

/**
 * [BaseLongIdTable]의 timestamp 필드(created_at, updated_at) 없는 버전
 *
 * @see BaseLongIdTable
 */
abstract class BaseLongIdTableWithoutTimestamp(
    name: String,
    idName: String = "id",
) : LongIdTable(name, idName)

/**
 * [BaseLongIdTable]의 UPDATE 구문에서 updated_at을 자동으로 갱신하는 확장 함수
 *
 * ##### 사용 예시
 * ```
 * Users.updateWithTimestamp({ Users.id eq userId }) {
 *     it[name] = "변경된이름"
 * }
 * ```
 */
fun <T : BaseLongIdTable> T.updateWithTimestamp(
    where: SqlExpressionBuilder.() -> Op<Boolean>,
    body: T.(UpdateStatement) -> Unit,
): Int = update(where) {
    body(it)
    // 이미 명시적으로 updated_at이 세팅된 경우 덮어쓰지 않음
    if (updatedAt !in it.firstDataSet.map { col -> col.first }) {
        it[updatedAt] = PeekrDateTime.now().toOffsetDateTime()
    }
}

/**
 * [BaseLongIdTable]의 UPSERT 구문에서 updated_at을 자동으로 갱신하는 확장 함수
 *
 * INSERT/UPDATE 모두 updated_at이 현재 시각으로 자동 갱신된다.
 * INSERT 시 created_at은 직접 세팅해야 한다.
 *
 * ##### 사용 예시
 * ```
 * Users.upsertWithTimestamp(Users.provider, Users.providerId) {
 *     it[provider] = SocialLoginProvider.KAKAO
 *     it[providerId] = "provider_id"
 *     it[name] = "변경된이름"
 *     it[createdAt] = PeekrDateTime.now().toOffsetDateTime() // INSERT 시 직접 세팅
 * }
 * ```
 */
fun <T : BaseLongIdTable> T.upsertWithTimestamp(
    vararg keys: Column<*>,
    onUpdateExclude: List<Column<*>>? = null,
    where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
    body: T.(UpsertStatement<Long>) -> Unit,
) = upsert(
    *keys,
    onUpdateExclude = onUpdateExclude,
    where = where,
) {
    body(it)
    // INSERT/UPDATE 모두 현재 시각으로 갱신
    it[updatedAt] = PeekrDateTime.now().toOffsetDateTime()
}

// ------------------------------ BaseEntity ------------------------------

/**
 * 모든 Entity의 기초가 되는 추상 클래스
 *
 * (단수형으로 정의된) 엔티티에서 사용
 *
 * ##### 사용 예시
 * ```
 * class User(id: EntityID<Long>) : BaseEntity(id, Users) {
 *     companion object : BaseEntityClass<User>(Users)
 *
 *     val name by Users.name
 *     val profileImageUrl by Users.profileImageUrl
 *     val introduce by Users.introduce
 * }
 * ```
 */
abstract class BaseEntity(
    id: EntityID<Long>,
    table: BaseLongIdTable,
) : LongEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

/**
 * [BaseEntity]의 timestamp 필드(created_at, updated_at) 없는 버전
 *
 * @see BaseEntity
 */
abstract class BaseEntityWithoutTimestamp(id: EntityID<Long>) : LongEntity(id)

/**
 * 공통적으로 updatedAt 필드를 자동으로 갱신하는 기능을 부여하는 공통 베이스 클래스
 *
 * Entity가 업데이트 될 때마다 이벤트를 수신해서 자동으로 updateAt을 갱신한다. (현재 시각 기준)
 *
 * ##### 사용 예시
 * ```
 * class User(id: EntityID<Long>) : BaseEntity(id, Users) {
 *     // here...
 *     companion object : BaseEntityClass<User>(Users)
 *
 *     val name by Users.name
 *     val profileImageUrl by Users.profileImageUrl
 *     val introduce by Users.introduce
 * }
 * ```
 */
abstract class BaseEntityClass<E : BaseEntity>(table: BaseLongIdTable) : LongEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    val entity = action.toEntity(this)
                    if (entity != null) {
                        entity.updatedAt = PeekrDateTime.now().toOffsetDateTime()
                    } else {
                        LOGGER.warn(
                            "Failed to update updatedAt: " +
                                "entity resolution failed for ${table.tableName} (action=$action)",
                        )
                    }
                } catch (e: Exception) {
                    LOGGER.warn("Failed to update updatedAt for ${table.tableName} (action=$action): ${e.message}")
                }
            }
        }
    }
}

/**
 * 타임스탬프 기능이 없는 베이스 엔티티 클래스
 */
abstract class BaseEntityClassWithoutTimestamp<E : BaseEntityWithoutTimestamp>(table: BaseLongIdTableWithoutTimestamp) :
    LongEntityClass<E>(table)

private val LOGGER = KtorSimpleLogger("BaseEntity")
