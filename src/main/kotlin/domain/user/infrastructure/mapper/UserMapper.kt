package com.peekr.domain.user.infrastructure.mapper

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.UserEntity
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.core.model.UserId
import com.peekr.domain.user.domain.model.RoleForUser
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser
import com.peekr.domain.user.domain.model.User

object UserMapper {
    /** ##### 반드시 db transaction 범위 내에서 실행되어야 한다. */
    fun toDomain(entity: UserEntity): User = User(
        id = UserId(entity.id.value),
        role = entity.role.toRoleForUser(),
        provider = entity.provider.toSocialLoginProviderForUser(),
        providerId = entity.providerId,
        displayId = entity.displayId,
        name = entity.name,
        profileImageUrl = entity.profileImageUrl,
        introduce = entity.introduce,
        isActive = entity.isActive,
        lastLoginAt = entity.lastLoginAt,
    )
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
