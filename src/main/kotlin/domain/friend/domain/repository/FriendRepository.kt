package com.turnin.domain.friend.domain.repository

import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.id.UserId
import com.turnin.domain.friend.domain.model.Friend
import com.turnin.domain.friend.domain.model.FriendFcmContext
import com.turnin.domain.friend.domain.model.FriendRequestContext
import com.turnin.domain.friend.domain.model.FriendsPagingData
import com.turnin.domain.friend.domain.model.IncomingRequestPagingData
import com.turnin.domain.friend.domain.model.UserInfo

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
     * 나에게 들어온 친구 요청 목록 조회 (페이지네이션)
     *
     * 목록은 **최신순**으로 정렬된다.
     *
     * @param userId 사용자 ID
     * @param offset 페이지 오프셋
     * @param size 페이지 사이즈
     */
    suspend fun getIncomingRequests(
        userId: UserId,
        offset: Long,
        size: Int,
    ): IncomingRequestPagingData

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
     * 친구 요청에 필요한 컨텍스트를 조회한다.
     * 요청자/수신자 정보 + 차단 관계를 한 번에 조회한다.
     *
     * @param requesterId 요청자 ID
     * @param receiverId 수신자 ID
     * @return [FriendRequestContext] or null (수신자가 존재하지 않는 경우)
     */
    suspend fun getFriendRequestContext(
        requesterId: UserId,
        receiverId: UserId,
    ): FriendRequestContext?

    /**
     * 친구들의 FCM 알림 전송에 필요한 컨텍스트를 조회한다.
     * 발신자 이름 + 친구들의 최신 활성 FCM 토큰을 한 번에 조회한다.
     * 친구당 가장 최근 updated_at 기준 토큰 1개만 선택하며 최대 [limit]명까지 조회한다.
     *
     * @param userId 발신자 ID
     * @param limit 조회할 친구 수
     * @return [FriendFcmContext]
     */
    suspend fun getFriendFcmContext(
        userId: UserId,
        limit: Int = FriendFcmContext.MAX_NOTIFICATION_RECIPIENTS,
    ): FriendFcmContext

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
     * @param updaterId 상태를 수정할 사용자 ID (원래 수신자)
     * @param requesterId 원래 요청을 보낸 사용자 ID
     * @param requestStatus 친구 요청 상태
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun updateFriendRequestStatus(
        updaterId: UserId,
        requesterId: UserId,
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

    /**
     * 사용자의 모든 친구 관계 삭제
     *
     * ###### 해당 메서드는 [userId]와 연결된 친구 관계 데이터를 모두 삭제하므로 주의해서 사용해야 한다.
     *
     * @param userId 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId)

    /**
     * 사용자 ID를 통해 사용자가 있는지 확인한다.
     *
     * **순환 참조를 방지하기 위해 임시방편으로 Users 테이블 조회만 수행한다.**
     */
    suspend fun existsUser(userId: UserId): Boolean

    /**
     * 사용자들의 ID를 통해 사용자 정보 일부 목록을 조회한다.
     * (방어 쿼리 추가: 비활성화 사용자 제외)
     *
     * **순환 참조를 방지하기 위해 임시방편으로 Users 테이블 조회만 수행한다.**
     */
    suspend fun getUserInfos(userIds: List<UserId>): List<UserInfo>
}
