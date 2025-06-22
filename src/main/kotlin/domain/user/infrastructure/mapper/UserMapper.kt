package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.user.domain.model.User

object UserMapper {
    /** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        provider = entity.provider.name,
        providerId = entity.providerId,
        name = entity.name,
        nickname = entity.nickname,
        profileImageUrl = entity.profileImageUrl,
        introduce = entity.introduce,
    )
}
