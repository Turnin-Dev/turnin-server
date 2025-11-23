package com.peekr.domain.user.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.application.provider.FriendProviderApi
import com.peekr.domain.friend.domain.model.FriendshipStatus
import com.peekr.domain.user.domain.provider.ExternalFriendshipStatus
import com.peekr.domain.user.domain.provider.FriendProvider

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun countFriends(userId: UserId): Long =
        friendProviderApi.countFriends(userId)

    override suspend fun getFriendshipStatus(
        userId: UserId,
        otherUserId: UserId,
    ): ExternalFriendshipStatus =
        when (val friendshipStatus = friendProviderApi.getFriendshipStatus(userId, otherUserId)) {
            FriendshipStatus.NOTHING -> ExternalFriendshipStatus.NOTHING
            FriendshipStatus.FRIENDS -> ExternalFriendshipStatus.FRIENDS
            FriendshipStatus.REQUESTED -> ExternalFriendshipStatus.REQUESTED
            FriendshipStatus.RECEIVED -> ExternalFriendshipStatus.RECEIVED
            else -> throw IllegalStateException("Unknown friendship status: $friendshipStatus")
        }
}
