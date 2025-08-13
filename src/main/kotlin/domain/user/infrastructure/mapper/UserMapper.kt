package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.user.domain.model.User

object UserMapper {
    /** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        role = entity.role.name,
        provider = entity.provider.name,
        providerId = entity.providerId,
        displayId = entity.displayId,
        name = entity.name,
        profileImageUrl = entity.profileImageUrl,
        introduce = entity.introduce,
    )
}
