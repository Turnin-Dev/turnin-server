package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.scheme.Users
import com.peekr.domain.user.domain.model.User
import org.jetbrains.exposed.sql.ResultRow

object UserMapper {
    fun toDomain(row: ResultRow): User = User(
        id = row[Users.id].value,
        provider = row[Users.provider].name,
        providerId = row[Users.providerId],
        name = row[Users.name],
        nickname = row[Users.nickname],
        profileImageUrl = row[Users.profileImageUrl],
        introduce = row[Users.introduce],
    )
}
