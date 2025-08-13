package com.peekr.domain.auth.infrastructure.mapper

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.Users
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import org.jetbrains.exposed.sql.ResultRow

object AuthMapper {
    fun toDomain(row: ResultRow): AuthUser =
        AuthUser(
            id = row[Users.id].value,
            role = row[Users.role].toRoleForAuth(),
            provider = row[Users.provider].toSocialLoginProviderForAuth(),
            providerId = row[Users.providerId],
            displayId = row[Users.displayId],
            name = row[Users.name],
            profileImageUrl = row[Users.profileImageUrl],
            introduce = row[Users.introduce],
        )
}

private fun Role.toRoleForAuth(): RoleForAuth = when (this) {
    Role.USER -> RoleForAuth.USER
    Role.ADMIN -> RoleForAuth.ADMIN
}

fun RoleForAuth.toRole(): Role = when (this) {
    RoleForAuth.USER -> Role.USER
    RoleForAuth.ADMIN -> Role.ADMIN
}

private fun SocialLoginProvider.toSocialLoginProviderForAuth(): SocialLoginProviderForAuth = when (this) {
    SocialLoginProvider.GOOGLE -> SocialLoginProviderForAuth.GOOGLE
    SocialLoginProvider.KAKAO -> SocialLoginProviderForAuth.KAKAO
    SocialLoginProvider.APPLE -> SocialLoginProviderForAuth.APPLE
}

fun SocialLoginProviderForAuth.toSocialLoginProvider(): SocialLoginProvider = when (this) {
    SocialLoginProviderForAuth.GOOGLE -> SocialLoginProvider.GOOGLE
    SocialLoginProviderForAuth.KAKAO -> SocialLoginProvider.KAKAO
    SocialLoginProviderForAuth.APPLE -> SocialLoginProvider.APPLE
}
