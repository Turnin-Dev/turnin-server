package com.turnin.domain.block.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.repository.BlockRepository

/**
 * 외부에 제공할 Block API
 */
class BlockProviderApi(private val blockRepository: BlockRepository) {
    /**
     * 차단 관계 여부를 확인한다.
     *
     * 둘 중 한 명이라도 서로를 차단한 관계라면 `true`를 반환하고 아니라면 `false`를 반환한다.
     * (같은 사용자인 경우 `false`)
     *
     * @param userId1 차단 관계 사용자 1 ID
     * @param userId2 차단 관계 사용자 2 ID
     */
    suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        blockRepository.isBlockedRelationship(userId1, userId2)
}
