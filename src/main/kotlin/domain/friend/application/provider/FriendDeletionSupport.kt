package com.peekr.domain.friend.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 외부에 제공할 Friend 삭제 제공 API
 */
class FriendDeletionSupport(private val friendRepository: FriendRepository) {
    /**
     * 사용자의 모든 친구 관계 삭제
     *
     * @param userId 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId) =
        friendRepository.deleteAll(userId)
}
