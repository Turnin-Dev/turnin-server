package com.turnin.domain.user.infrastructure.mapper

import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Introduce
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.domain.model.User
import org.jetbrains.exposed.sql.ResultRow

/** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
object UserMapper {
    fun UserEntity.toDomain(): User = User(
        id = UserId(this.id.value),
        role = this.role,
        provider = this.provider,
        providerId = this.providerId,
        displayId = DisplayId(this.displayId),
        userName = UserName(this.name),
        profileImageUrl = this.profileImageUrl,
        introduce = Introduce(introduce),
        isActive = this.isActive,
        lastLoginAt = this.lastLoginAt,
    )

    fun ResultRow.toDomain(isBlocked: Boolean): User = User(
        id = UserId(this[Users.id].value),
        role = this[Users.role],
        provider = this[Users.provider],
        providerId = this[Users.providerId],
        displayId = DisplayId(this[Users.displayId]),
        userName = UserName(this[Users.name]),
        profileImageUrl = this[Users.profileImageUrl],
        introduce = Introduce(this[Users.introduce]),
        isActive = this[Users.isActive],
        lastLoginAt = this[Users.lastLoginAt],
        isBlocked = isBlocked,
    )
}
