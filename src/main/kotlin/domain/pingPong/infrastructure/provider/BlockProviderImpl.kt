package com.turnin.domain.pingPong.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.block.application.provider.BlockProviderApi
import com.turnin.domain.pingPong.domain.provider.BlockProvider

class BlockProviderImpl(private val blockProviderApi: BlockProviderApi) : BlockProvider {
    override suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        blockProviderApi.isBlockedRelationship(userId1, userId2)
}
