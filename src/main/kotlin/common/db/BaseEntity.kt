package com.peekr.common.db

import com.peekr.common.util.PeekrDateTime
import io.ktor.util.logging.KtorSimpleLogger
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.toEntity
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * 모든 `LongIdTable`의 기초가 되는 추상 클래스
 *
 * Exposed의 DAO 스타일의 테이블 명세(복수형으로 정의된 테이블)에서 사용
 *
 * ##### 사용 예시
 * ```
 * object Users : BaseLongIdTable("user") {
 *     val name = varchar("name", 50)
 *     val nickname = varchar("nickname", 50).nullable()
 *     val profileImageUrl = varchar("profile_image_url", 500).nullable()
 *     val introduce = text("introduce").nullable()
 * }
 * ```
 */
abstract class BaseLongIdTable(
    name: String,
    idName: String = "id",
) : LongIdTable(name, idName) {
    val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(PeekrDateTime.timestamp)
}

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
 *     val nickname by Users.nickname
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
 *     val nickname by Users.nickname
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
                    action.toEntity(this)?.updatedAt = PeekrDateTime.now()
                } catch (e: Exception) {
                    LOGGER.warn("Failed to update entity $this updatedAt:\n${e.message}")
                }
            }
        }
    }
}

private val LOGGER = KtorSimpleLogger("BaseEntity")
