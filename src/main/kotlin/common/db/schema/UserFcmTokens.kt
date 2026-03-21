package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** FCM 토큰 엔티티 클래스 (Exposed DSL 방식) */
object UserFcmTokens : BaseLongIdTable("user_fcm_token") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val token = text("token")
    val isActive = bool("is_active").default(true)

    init {
        uniqueIndex("uq_fcm_token", token)
        index("idx_fcm_token_user_id", false, userId)
        index("idx_fcm_token_active", false, userId, isActive)
    }
}

/** FCM 토큰 엔티티 클래스 (Exposed DAO/ORM 방식) */
class UserFcmTokenEntity(id: EntityID<Long>) : BaseEntity(id, UserFcmTokens) {
    companion object : BaseEntityClass<UserFcmTokenEntity>(UserFcmTokens)

    var userId by UserFcmTokens.userId
    var token by UserFcmTokens.token
    var isActive by UserFcmTokens.isActive
}
