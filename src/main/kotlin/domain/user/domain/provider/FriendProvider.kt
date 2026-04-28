package com.turnin.domain.user.domain.provider

import com.turnin.common.model.FriendStatus
import com.turnin.common.model.id.UserId

/**
 * 외부에서 제공되는 Friend BC API 인터페이스
 */
interface FriendProvider {
    /**
     * 친구 수 조회
     *
     * @param userId 사용자 ID
     *
     * @return [Long]타입의 친구 수
     */
    suspend fun countFriends(userId: UserId): Long

    /**
     * 친구 상태 조회
     *
     * @param userId 나의 사용자 ID
     * @param otherUserId 다른 사용자 ID
     */
    suspend fun getFriendStatus(
        userId: UserId,
        otherUserId: UserId,
    ): FriendStatus
}
