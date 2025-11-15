package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.user.domain.model.RoleForUser
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserProfile
import org.jetbrains.exposed.sql.ResultRow

/** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = UserId(entity.id.value),
        role = entity.role.toRoleForUser(),
        provider = entity.provider.toSocialLoginProviderForUser(),
        providerId = entity.providerId,
        displayId = DisplayId(entity.displayId),
        name = Name(entity.name),
        profileImageUrl = entity.profileImageUrl,
        introduce = entity.introduce?.let { Introduce(it) },
        isActive = entity.isActive,
        lastLoginAt = entity.lastLoginAt,
    )

    fun toDomain(row: ResultRow): User = User(
        id = UserId(row[Users.id].value),
        role = row[Users.role].toRoleForUser(),
        provider = row[Users.provider].toSocialLoginProviderForUser(),
        providerId = row[Users.providerId],
        displayId = DisplayId(row[Users.displayId]),
        name = Name(row[Users.name]),
        profileImageUrl = row[Users.profileImageUrl],
        introduce = row[Users.introduce]?.let { Introduce(it) },
        isActive = row[Users.isActive],
        lastLoginAt = row[Users.lastLoginAt],
    )

    fun toDomain(user: User, friendsCount: Long): UserProfile =
        UserProfile(user, friendsCount)
}

fun Role.toRoleForUser(): RoleForUser = when (this) {
    Role.USER -> RoleForUser.USER
    Role.ADMIN -> RoleForUser.ADMIN
}

fun Role.toRoleForAuth(): RoleForAuth = when (this) {
    Role.USER -> RoleForAuth.USER
    Role.ADMIN -> RoleForAuth.ADMIN
}

fun SocialLoginProvider.toSocialLoginProviderForUser(): SocialLoginProviderForUser = when (this) {
    SocialLoginProvider.GOOGLE -> SocialLoginProviderForUser.GOOGLE
    SocialLoginProvider.KAKAO -> SocialLoginProviderForUser.KAKAO
    SocialLoginProvider.APPLE -> SocialLoginProviderForUser.APPLE
}

fun SocialLoginProviderForUser.toSocialLoginProvider(): SocialLoginProvider = when (this) {
    SocialLoginProviderForUser.GOOGLE -> SocialLoginProvider.GOOGLE
    SocialLoginProviderForUser.KAKAO -> SocialLoginProvider.KAKAO
    SocialLoginProviderForUser.APPLE -> SocialLoginProvider.APPLE
}
