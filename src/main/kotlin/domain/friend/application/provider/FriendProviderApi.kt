package com.peekr.domain.friend.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 외부에 제공할 Friend API
 */
class FriendProviderApi(private val friendRepository: FriendRepository) {
    /**
     * 친구 수 조회
     *
     * @param userId 사용자 ID
     *
     * @return [Long]타입의 친구 수
     */
    suspend fun countFriends(userId: UserId): Long =
        friendRepository.countFriends(userId)
}
