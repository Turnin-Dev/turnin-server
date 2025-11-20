package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.UserId
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 친구 추가(요청)
 */
class AddFriendUseCase(
    private val friendRepository: FriendRepository,
    private val userProvider: UserProvider,
) {
    /**
     * 친구 추가(요청)
     *
     * @param requesterId 친구 요청한 사용자 ID
     * @param receiverId 친구 요청받을 사용자 ID
     *
     * @return [FriendDto]
     */
    suspend operator fun invoke(
        requesterId: Long,
        receiverId: Long,
    ): FriendDto {
        val requesterIdVO = UserId(requesterId)
        val receiverIdVO = UserId(receiverId)

        // 1) 사용자가 존재하지 않으면 요청을 할 수 없다.
        if (!userProvider.existsUser(UserId(requesterId))) {
            throw FriendException.UserNotFoundException()
        }

        // 2) 본인에게 친구 추가를 할 수 없다.
        if (requesterId == receiverId) {
            throw FriendException.SelfRequestException()
        }

        // 3) 이미 친구 요청을 했거나 친구 상태인 경우
        return try {
            friendRepository.createFriend(requesterIdVO, receiverIdVO).toDto()
        } catch (e: DatabaseException.DuplicatedDataException) {
            throw FriendException.AlreadyFriendRequestException()
        }
    }
}
