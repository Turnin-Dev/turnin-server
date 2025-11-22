package com.peekr.domain.friend.domain.repository

import com.peekr.common.model.FriendStatus
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.Friend

interface FriendRepository {
    /**
     * 친구 목록 조회
     *
     * @param userId 사용자(본인) ID
     */
    suspend fun getFriends(userId: UserId): List<Friend>

    /**
     * 친구 수 조회
     *
     * @param userId 사용자 ID
     *
     * @return [Long]타입의 친구 수
     */
    suspend fun countFriends(userId: UserId): Long

    /**
     * 친구 요청 생성
     *
     * @param requesterId 요청한 사용자 ID
     * @param receiverId 요청 받을 사용자 ID
     *
     * @return [Friend] 친구 엔티티 모델
     */
    suspend fun createFriend(
        requesterId: UserId,
        receiverId: UserId,
    ): Friend

    /**
     * 친구 상태 수정
     *
     * @param userId1 사용자 ID (수정 요청 주체)
     * @param userId2 사용자 ID (수정 대상 친구)
     * @param status 친구 상태
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun updateFriendStatus(
        userId1: UserId,
        userId2: UserId,
        status: FriendStatus,
    ): Boolean

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
    ): Boolean
}
