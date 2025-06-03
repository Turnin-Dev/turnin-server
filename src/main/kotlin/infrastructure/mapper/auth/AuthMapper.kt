package com.peekr.infrastructure.mapper.auth

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.infrastructure.persistence.Users
import org.jetbrains.exposed.sql.ResultRow

object AuthMapper {
    fun toDomain(row: ResultRow): AuthUser =
        AuthUser(
            id = row[Users.id].value,
            provider = row[Users.provider],
            providerId = row[Users.providerId],
            name = row[Users.name],
            nickname = row[Users.nickname] ?: "",
            profileImageUrl = row[Users.profileImageUrl] ?: "",
            introduce = row[Users.introduce] ?: "",
        )
}
