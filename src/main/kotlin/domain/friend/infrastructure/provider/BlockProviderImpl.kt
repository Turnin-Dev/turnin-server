package com.peekr.domain.friend.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.block.application.provider.BlockProviderApi
import com.peekr.domain.friend.domain.provider.BlockProvider

class BlockProviderImpl(private val blockProviderApi: BlockProviderApi) : BlockProvider {
    override suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        blockProviderApi.isBlockedRelationship(userId1, userId2)
}
