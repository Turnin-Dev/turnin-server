package com.peekr.domain.friend.domain.repository

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendsPagingData
import com.peekr.domain.friend.domain.model.IncomingRequesterPagingData

interface FriendRepository {
    /**
     * 친구 목록 조회 (페이지네이션)
     *
     * @param userId 사용자 ID
     * @param offset 페이지 오프셋
     * @param size 페이지 사이즈
     */
    suspend fun getFriendsPagination(
        userId: UserId,
        offset: Long,
        size: Int,
    ): FriendsPagingData

    /**
     * 받은 친구 요청 목록 조회 (페이지네이션)
     *
     * 목록은 **최신순**으로 정렬된다.
     *
     * @param userId 사용자 ID
     * @param offset 페이지 오프셋
     * @param size 페이지 사이즈
     */
    suspend fun getIncomingRequesters(
        userId: UserId,
        offset: Long,
        size: Int,
    ): IncomingRequesterPagingData

    /**
     * 사용자(본인) ID와 다른 사용자 ID로 친구 데이터를 조회한다.
     *
     * @param userId 사용자(본인) ID
     * @param otherUserId 다른 사용자 ID
     */
    suspend fun findByIds(
        userId: UserId,
        otherUserId: UserId,
    ): Friend?

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
     * 친구 요청 상태 수정
     *
     * @param userId1 사용자 ID (수정 요청 주체)
     * @param userId2 사용자 ID (수정 대상 친구)
     * @param requestStatus 친구 요청 상태
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun updateFriendRequestStatus(
        userId1: UserId,
        userId2: UserId,
        requestStatus: FriendRequestStatus,
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
