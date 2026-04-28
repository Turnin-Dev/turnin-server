package com.turnin.domain.block.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.provider.FriendProvider
import com.turnin.domain.friend.application.provider.FriendProviderApi

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun deleteFriend(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        friendProviderApi.deleteFriend(userId1, userId2)
}
