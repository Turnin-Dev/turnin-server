package com.peekr.domain.discover.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.discover.domain.provider.ExternalUser
import com.peekr.domain.discover.domain.provider.UserProvider
import com.peekr.domain.user.application.provider.UserProviderApi

class UserProviderImpl(private val userProviderApi: UserProviderApi) : UserProvider {
    override suspend fun findByIds(ids: List<UserId>): List<ExternalUser> =
        userProviderApi.findByIds(ids).map {
            ExternalUser(
                id = it.id,
                role = it.role,
                provider = it.provider,
                providerId = it.providerId,
                displayId = it.displayId,
                name = it.userName,
                profileImageUrl = it.profileImageUrl,
                introduce = it.introduce,
                isActive = it.isActive,
                lastLoginAt = it.lastLoginAt,
            )
        }
}
