package com.peekr.domain.block.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.repository.BlockRepository

/**
 * 외부에 제공할 Block API
 */
class BlockProviderApi(private val repository: BlockRepository) {
    /**
     * 차단 사용자 여부 확인
     *
     * [userId1]과 [userId2]의 차단 관계를 확인한다.
     *
     * 둘 중 한 명이라도 서로를 차단한 관계라면 `true`를 반환하고 아니라면 `false`를 반환한다.
     *
     * @param userId1 차단 관계 사용자 1 ID
     * @param userId2 차단 관계 사용자 2 ID
     */
    suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        repository.isBlockedRelationship(userId1, userId2)
}
