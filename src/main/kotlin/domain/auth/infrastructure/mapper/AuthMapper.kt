package com.peekr.domain.auth.infrastructure.mapper

import com.peekr.common.db.schema.Users
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.auth.domain.model.AuthUser
import org.jetbrains.exposed.sql.ResultRow

object AuthMapper {
    fun toDomain(row: ResultRow): AuthUser =
        AuthUser(
            userId = UserId(row[Users.id].value),
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
