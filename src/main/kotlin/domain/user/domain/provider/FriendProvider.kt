package com.peekr.domain.user.domain.provider

import com.peekr.common.model.id.UserId

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
}
