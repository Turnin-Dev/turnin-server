package com.peekr.domain.auth.infrastructure.mapper

import com.peekr.common.db.scheme.Users
import com.peekr.domain.auth.domain.model.AuthUser
import org.jetbrains.exposed.sql.ResultRow

object AuthMapper {
    fun toDomain(row: ResultRow): AuthUser =
        AuthUser(
            id = row[Users.id].value,
            role = row[Users.role],
            provider = row[Users.provider],
            providerId = row[Users.providerId],
            displayId = row[Users.displayId],
            name = row[Users.name],
            profileImageUrl = row[Users.profileImageUrl] ?: "",
            introduce = row[Users.introduce] ?: "",
        )
}
