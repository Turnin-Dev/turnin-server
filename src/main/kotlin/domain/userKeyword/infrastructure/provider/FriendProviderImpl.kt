package com.turnin.domain.userKeyword.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.friend.application.provider.FriendProviderApi
import com.turnin.domain.userKeyword.domain.model.UserKeywordFriendFcmContext
import com.turnin.domain.userKeyword.domain.provider.FriendProvider

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun getFriendFcmContext(userId: UserId): UserKeywordFriendFcmContext {
        val context = friendProviderApi.getFriendFcmContext(userId)
        return UserKeywordFriendFcmContext(
            friendTokens = context.friendTokens,
            senderName = context.senderName,
        )
    }
}
