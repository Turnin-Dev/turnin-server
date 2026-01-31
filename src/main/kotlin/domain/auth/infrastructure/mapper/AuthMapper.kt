package com.peekr.domain.auth.infrastructure.mapper

import com.peekr.common.db.schema.Users
import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
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
            userName = UserName(row[Users.name]),
            profileImageUrl = row[Users.profileImageUrl],
            introduce = Introduce(row[Users.introduce]),
            isActive = row[Users.isActive],
            lastLoginAt = row[Users.lastLoginAt],
        )
}
