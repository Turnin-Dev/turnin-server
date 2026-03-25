package com.peekr.domain.userKeyword.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.application.provider.FriendProviderApi
import com.peekr.domain.userKeyword.domain.model.UserKeywordFriendFcmContext
import com.peekr.domain.userKeyword.domain.provider.FriendProvider

class FriendProviderImpl(private val friendProviderApi: FriendProviderApi) : FriendProvider {
    override suspend fun getFriendFcmContext(userId: UserId): UserKeywordFriendFcmContext {
        val context = friendProviderApi.getFriendFcmContext(userId)
        return UserKeywordFriendFcmContext(
            friendTokens = context.friendTokens,
            senderName = context.senderName,
        )
    }
}
