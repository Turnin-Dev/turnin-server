package com.peekr.domain.friend.domain.provider

import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공되는 차단 BC API 인터페이스
 */
interface BlockProvider {
    suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean
}
