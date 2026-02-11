package com.peekr.domain.block.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.provider.FriendProvider
import com.peekr.domain.friend.application.provider.FriendProviderApi

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun deleteFriend(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        friendProviderApi.deleteFriend(userId1, userId2)
}
