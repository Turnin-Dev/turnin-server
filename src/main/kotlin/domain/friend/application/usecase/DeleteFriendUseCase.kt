package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 삭제
 */
class DeleteFriendUseCase(private val friendRepository: FriendRepository) {
    /**
     * 친구 삭제 및 친구 요청 삭제 한다.
     *
     * @param userId1 사용자 ID (삭제 요청 주체)
     * @param userId2 사용자 ID (삭제 대상 친구)
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend operator fun invoke(
        userId1: Long,
        userId2: Long,
    ): Boolean {
        val userId1VO = UserId(userId1)
        val userId2VO = UserId(userId2)
        return friendRepository.deleteFriend(userId1VO, userId2VO)
    }
}
