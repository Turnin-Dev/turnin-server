package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Users : BaseLongIdTable("user") {
    val provider = enumerationByName("provider", 50, SocialLoginProvider::class)
    val providerId = varchar("provider_id", 255)
    val name = varchar("name", 50)
    val nickname = varchar("nickname", 50)
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val introduce = text("introduce").nullable()

    init {
        uniqueIndex("unique_provider_user", provider, providerId)
    }
}

// 엔티티 정의 (단수형 정의)
class UserEntity(id: EntityID<Long>) : BaseEntity(id, Users) {
    companion object : BaseEntityClass<UserEntity>(Users)

    var provider by Users.provider
    var providerId by Users.providerId
    var name by Users.name
    var nickname by Users.nickname
    var profileImageUrl by Users.profileImageUrl
    var introduce by Users.introduce
}
