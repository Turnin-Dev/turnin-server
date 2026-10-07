package com.turnin.domain.pingPong.infrastructure.provider

import com.turnin.common.model.UserName
import com.turnin.common.model.id.UserId
import com.turnin.domain.pingPong.domain.provider.UserProvider
import com.turnin.domain.user.application.provider.UserProviderApi

class UserProviderImpl(private val userProviderApi: UserProviderApi) : UserProvider {
    override suspend fun findUserName(userId: UserId): UserName? =
        userProviderApi
            .findByIds(listOf(userId))
            .singleOrNull()
            ?.userName
}
