package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.common.db.DatabaseUtils.customPostgresEnum
import org.jetbrains.exposed.dao.id.EntityID

object Users : BaseLongIdTable("user") {
    val role = customPostgresEnum<Role>("role", "user_role").default(Role.USER)
    val provider = customPostgresEnum<SocialLoginProvider>("provider", "social_login_provider")
    val providerId = varchar("provider_id", 255)
    val displayId = varchar("display_id", 30)
    val name = varchar("name", 30)
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val introduce = text("introduce").nullable()

    init {
        uniqueIndex("uq_provider_user", provider, providerId)
        uniqueIndex("uq_display_id", displayId)
    }
}

// 엔티티 정의 (단수형 정의)
class UserEntity(id: EntityID<Long>) : BaseEntity(id, Users) {
    companion object : BaseEntityClass<UserEntity>(Users)

    var role by Users.role
    var provider by Users.provider
    var providerId by Users.providerId
    var displayId by Users.displayId
    var name by Users.name
    var profileImageUrl by Users.profileImageUrl
    var introduce by Users.introduce
}
