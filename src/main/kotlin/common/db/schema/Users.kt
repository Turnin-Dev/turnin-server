package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.common.db.DatabaseUtils.customPostgresEnum
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.timestamp

/** 사용자 엔티티 클래스 (Exposed DSL 방식) */
object Users : BaseLongIdTable("user") {
    val role = customPostgresEnum<Role>("role", "user_role").default(Role.USER)
    val provider = customPostgresEnum<SocialLoginProvider>("provider", "social_login_provider")
    val providerId = varchar("provider_id", 255)
    val displayId = varchar("display_id", 30).uniqueIndex("uq_users_display_id")
    val name = varchar("name", 30)
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val introduce = text("introduce")
    val isActive = bool("is_active").default(true)
    val lastLoginAt = timestamp("last_login_at")

    init {
        uniqueIndex("uq_provider_user", provider, providerId)
    }
}

/** 사용자 엔티티 클래스 (Exposed DAO/ORM 방식) */
class UserEntity(id: EntityID<Long>) : BaseEntity(id, Users) {
    companion object : BaseEntityClass<UserEntity>(Users)

    var role by Users.role
    var provider by Users.provider
    var providerId by Users.providerId
    var displayId by Users.displayId
    var name by Users.name
    var profileImageUrl by Users.profileImageUrl
    var introduce by Users.introduce
    var isActive by Users.isActive
    var lastLoginAt by Users.lastLoginAt
}
