package com.turnin.domain.pingPong.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.userKeyword.application.provider.UserKeywordProviderApi

class UserKeywordProviderImpl(private val userKeywordProviderApi: UserKeywordProviderApi) : UserKeywordProvider {
    override suspend fun findOwnerId(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
    ): UserId? =
        userKeywordProviderApi.findOwnerId(currentUserId, userKeywordId)
}
