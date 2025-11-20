package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendStatus
import com.peekr.common.model.UserId
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 친구 상태 수정
 */
class UpdateFriendStatusUseCase(
    private val friendRepository: FriendRepository,
    private val userProvider: UserProvider,
) {
    /**
     * 친구 상태를 수정한다.
     *
     * @param userId1 사용자 ID (수정 요청 주체)
     * @param userId2 사용자 ID (수정 대상 친구)
     * @param status 친구 상태
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend operator fun invoke(
        userId1: Long,
        userId2: Long,
        status: FriendStatus,
    ): Boolean {
        val userId1VO = UserId(userId1)
        val userId2VO = UserId(userId2)

        // 1) 스스로 친구 관계가 될 수 없기에 상태를 변경할 수 없다.
        if (userId1 == userId2) {
            throw FriendException.SelfRequestException()
        }

        // 2) 상태를 수정하려는 친구(사용자)가 존재하지 않는 경우 수정이 불가능하다.
        if (!userProvider.existsUser(userId1VO) ||
            !userProvider.existsUser(userId2VO)
        ) {
            throw FriendException.UserNotFoundException()
        }

        return friendRepository.updateFriendStatus(userId1VO, userId2VO, status)
    }
}
