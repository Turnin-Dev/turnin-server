package com.peekr.domain.auth.infrastructure.mapper

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.Users
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import org.jetbrains.exposed.sql.ResultRow

object AuthMapper {
    fun toDomain(row: ResultRow): AuthUser =
        AuthUser(
            userId = UserId(row[Users.id].value),
            role = row[Users.role].toRoleForAuth(),
            provider = row[Users.provider].toSocialLoginProviderForAuth(),
            providerId = row[Users.providerId],
            displayId = DisplayId(row[Users.displayId]),
            name = Name(row[Users.name]),
            profileImageUrl = row[Users.profileImageUrl],
            introduce = row[Users.introduce]?.let { Introduce(it) },
            isActive = row[Users.isActive],
            lastLoginAt = row[Users.lastLoginAt],
        )
}

private fun Role.toRoleForAuth(): RoleForAuth = when (this) {
    Role.USER -> RoleForAuth.USER
    Role.ADMIN -> RoleForAuth.ADMIN
}

internal fun RoleForAuth.toRole(): Role = when (this) {
    RoleForAuth.USER -> Role.USER
    RoleForAuth.ADMIN -> Role.ADMIN
}

fun SocialLoginProvider.toSocialLoginProviderForAuth(): SocialLoginProviderForAuth = when (this) {
    SocialLoginProvider.GOOGLE -> SocialLoginProviderForAuth.GOOGLE
    SocialLoginProvider.KAKAO -> SocialLoginProviderForAuth.KAKAO
    SocialLoginProvider.APPLE -> SocialLoginProviderForAuth.APPLE
}

fun SocialLoginProviderForAuth.toSocialLoginProvider(): SocialLoginProvider = when (this) {
    SocialLoginProviderForAuth.GOOGLE -> SocialLoginProvider.GOOGLE
    SocialLoginProviderForAuth.KAKAO -> SocialLoginProvider.KAKAO
    SocialLoginProviderForAuth.APPLE -> SocialLoginProvider.APPLE
}
