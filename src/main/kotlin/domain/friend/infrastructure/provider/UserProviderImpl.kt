package com.peekr.domain.friend.infrastructure.provider

import com.peekr.common.model.UserId
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.user.application.provider.UserProviderApi

class UserProviderImpl(private val userProviderApi: UserProviderApi) : UserProvider {
    override suspend fun existsUser(userId: UserId): Boolean =
        userProviderApi.findById(userId) != null
}
