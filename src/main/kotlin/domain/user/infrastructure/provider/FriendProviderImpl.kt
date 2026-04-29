package com.turnin.domain.user.infrastructure.provider

import com.turnin.common.model.FriendStatus
import com.turnin.common.model.id.UserId
import com.turnin.domain.friend.application.provider.FriendProviderApi
import com.turnin.domain.user.domain.provider.FriendProvider

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun countFriends(userId: UserId): Long =
        friendProviderApi.countFriends(userId)

    override suspend fun getFriendStatus(
        userId: UserId,
        otherUserId: UserId,
    ): FriendStatus =
        friendProviderApi.getFriendStatus(userId, otherUserId)
}
