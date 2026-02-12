package com.peekr.domain.friend.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.provider.ExternalUserInfo
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.user.application.provider.UserProviderApi

class UserProviderImpl(private val userProviderApi: UserProviderApi) : UserProvider {
    override suspend fun existsUser(userId: UserId): Boolean =
        userProviderApi.existsUser(userId)

    override suspend fun getUserInfos(userIds: List<UserId>): List<ExternalUserInfo> {
        val users = userProviderApi.findByIds(userIds)
        return users.map { user ->
            ExternalUserInfo(
                userId = user.id,
                displayId = user.displayId,
                userName = user.userName,
                profileImageUrl = user.profileImageUrl,
            )
        }
    }
}
