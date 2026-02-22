package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 친구 추가(요청)
 */
class AddFriendUseCase(private val friendRepository: FriendRepository) {
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

        // 1) 차단 관계인 상태에서는 친구 요청을 할 수 없다.
        if (friendRepository.isBlockedRelationship(requesterIdVO, receiverIdVO)) {
            // 클라이언트에게 예외 사유로 차단 관계라는 것을 알리지 않기 위해 단순히 사용자를 찾을 수 없다는 예외를 보낸다.
            LOGGER.warn(
                "User already filtered by visibility check, but reached friend request logic" +
                    "(Requester ID: ${requesterIdVO.value}, Receiver ID: ${receiverIdVO.value})",
            )
            throw FriendException.UserNotFoundException()
        }

        // 2) 본인에게 친구 추가를 할 수 없다.
        if (requesterId == receiverId) {
            throw FriendException.SelfRequestException()
        }

        // 3) 요청 받을 사용자가 존재하지 않으면 요청을 할 수 없다.
        if (!friendRepository.existsUser(receiverIdVO)) {
            throw FriendException.UserNotFoundException()
        }

        // 4) 이미 친구 요청을 했거나 친구 상태인 경우
        return try {
            friendRepository.createFriend(requesterIdVO, receiverIdVO).toDto()
        } catch (e: DatabaseException.DuplicatedDataException) {
            throw FriendException.AlreadyFriendRequestException(e)
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<AddFriendUseCase>()
