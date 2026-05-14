package com.turnin.domain.auth.infrastructure.mapper

import com.turnin.common.db.schema.Users
import com.turnin.common.model.Introduce
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.model.AuthUser
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
