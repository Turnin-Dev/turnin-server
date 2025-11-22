package com.peekr.domain.user.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.application.provider.FriendProviderApi
import com.peekr.domain.user.domain.provider.FriendProvider

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun countFriends(userId: UserId): Long =
        friendProviderApi.countFriends(userId)
}
