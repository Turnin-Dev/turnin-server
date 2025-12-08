package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 상태 조회
 */
class GetFriendStatusUseCase(private val friendRepository: FriendRepository) {
    /**
     * 친구 상태를 조회한다.
     *
     * @param userId 나의 사용자 ID
     * @param otherUserId 다른 사용자 ID
     */
    suspend operator fun invoke(
        userId: UserId,
        otherUserId: UserId,
    ): FriendStatus {
        val friend = friendRepository.findByIds(userId, otherUserId)

        // 1. 아무 관계도 아닌 경우: Friends 테이블에 데이터 조차 없어야 한다.
        if (friend == null) {
            return FriendStatus.NOTHING
        }

        // 2. 친구 관계: userId, otherUserId가 requesterId - receiverId 쌍에 매칭되고 친구 상태가 ACCEPTED이어야 한다.
        val isRequesterReceiverRelation = isRequesterReceiverRelation(
            requesterId = friend.requesterId,
            receiverId = friend.receiverId,
            userId = userId,
            otherUserId = otherUserId,
        )
        if (isRequesterReceiverRelation && friend.requestStatus == FriendRequestStatus.ACCEPTED) {
            return FriendStatus.FRIENDS
        }

        // 3. 친구 요청을 보낸 상태: requesterId == userId, receiverId == otherUserId 이고 친구 상태가 PENDING이어야 한다.
        if (friend.requesterId == userId && friend.receiverId == otherUserId &&
            friend.requestStatus == FriendRequestStatus.PENDING
        ) {
            return FriendStatus.REQUESTED
        }

        // 4. 친구 요청을 받은 상태: requesterId == otherUserId, receiverId == userId 이고 친구 상태가 PENDING이어야 한다.
        if (friend.requesterId == otherUserId && friend.receiverId == userId &&
            friend.requestStatus == FriendRequestStatus.PENDING
        ) {
            return FriendStatus.RECEIVED
        }

        return FriendStatus.NOTHING
    }

    /**
     * 두 사용자가 서로 요청자, 수신자 관계인지 확인한다.
     */
    private fun isRequesterReceiverRelation(
        requesterId: UserId,
        receiverId: UserId,
        userId: UserId,
        otherUserId: UserId,
    ): Boolean =
        (requesterId == userId && receiverId == otherUserId) ||
            (requesterId == otherUserId && receiverId == userId)
}
