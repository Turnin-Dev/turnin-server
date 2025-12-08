package com.peekr.domain.friend.application.provider

import com.peekr.common.model.FriendStatus
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.application.usecase.GetFriendStatusUseCase
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 외부에 제공할 Friend API
 */
class FriendProviderApi(
    private val friendRepository: FriendRepository,
    private val getFriendStatusUseCase: GetFriendStatusUseCase,
) {
    /**
     * 친구 수 조회
     *
     * @param userId 사용자 ID
     *
     * @return [Long]타입의 친구 수
     */
    suspend fun countFriends(userId: UserId): Long =
        friendRepository.countFriends(userId)

    /**
     * 친구 상태 조회
     *
     * @param userId 사용자(본인) ID
     * @param otherUserId 다른 사용자 ID
     */
    suspend fun getFriendStatus(
        userId: UserId,
        otherUserId: UserId,
    ): FriendStatus =
        getFriendStatusUseCase(userId, otherUserId)
}
