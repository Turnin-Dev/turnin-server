package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import org.jetbrains.exposed.sql.ResultRow

/** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
object UserMapper {
    fun UserEntity.toDomain(): User = User(
        id = UserId(this.id.value),
        role = this.role,
        provider = this.provider,
        providerId = this.providerId,
        displayId = DisplayId(this.displayId),
        name = Name(this.name),
        profileImageUrl = this.profileImageUrl,
        introduce = this.introduce?.let { Introduce(it) },
        isActive = this.isActive,
        lastLoginAt = this.lastLoginAt,
    )

    fun toDomain(row: ResultRow): User = User(
        id = UserId(row[Users.id].value),
        role = row[Users.role],
        provider = row[Users.provider],
        providerId = row[Users.providerId],
        displayId = DisplayId(row[Users.displayId]),
        name = Name(row[Users.name]),
        profileImageUrl = row[Users.profileImageUrl],
        introduce = row[Users.introduce]?.let { Introduce(it) },
        isActive = row[Users.isActive],
        lastLoginAt = row[Users.lastLoginAt],
    )
}
