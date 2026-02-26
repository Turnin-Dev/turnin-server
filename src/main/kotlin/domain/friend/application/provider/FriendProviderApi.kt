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

    /**
     * 친구 삭제 및 친구 요청 삭제
     *
     * @param userId1 사용자 ID (삭제 요청 주체)
     * @param userId2 사용자 ID (삭제 대상 친구)
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun deleteFriend(
        userId1: UserId,
        userId2: UserId,
    ): Boolean =
        friendRepository.deleteFriend(userId1, userId2)

    /**
     * 사용자의 모든 친구 관계 삭제
     *
     * @param userId 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId) =
        friendRepository.deleteAll(userId)
}
