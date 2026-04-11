package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.suspendTransaction
import com.peekr.common.firebase.RefType
import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppDispatchers
import com.peekr.common.util.log.AppLoggerFactory
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.message.FriendNotificationMessage
import com.peekr.domain.friend.domain.model.FriendNotificationCommand
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 친구 추가(요청)
 */
class AddFriendUseCase(
    private val friendRepository: FriendRepository,
    private val notificationProvider: NotificationProvider,
    private val applicationScope: CoroutineScope,
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

        // 1) 본인에게 친구 추가를 할 수 없다.
        if (requesterId == receiverId) {
            throw FriendException.SelfRequestException()
        }

        val (friend, requesterName) = suspendTransaction {
            // 2) 요청자/수신자 정보 조회 + 차단 관계 확인
            val context = friendRepository.getFriendRequestContext(requesterIdVO, receiverIdVO)
                ?: throw FriendException.UserNotFoundException()

            // 3) 차단 관계인 경우 (보안상 사용자를 찾을 수 없다는 예외로 처리)
            if (context.isBlocked) {
                LOGGER.warn(
                    "User already filtered by visibility check, but reached friend request logic" +
                        "(Requester ID: ${requesterIdVO.value}, Receiver ID: ${receiverIdVO.value})",
                )
                throw FriendException.UserNotFoundException()
            }

            // 4) 친구 요청 생성
            val friend = try {
                friendRepository.createFriend(requesterIdVO, receiverIdVO).toDto()
            } catch (e: DatabaseException.DuplicatedDataException) {
                throw FriendException.AlreadyFriendRequestException(e)
            }

            friend to context.requesterInfo.userName.value
        }

        // 5) 친구 요청 알림 전송 비동기 실행 (실패해도 친구 요청은 성공으로 처리)
        applicationScope.launch(AppDispatchers.ioDispatcher) {
            runCatching {
                notificationProvider.sendNotification(
                    FriendNotificationCommand(
                        userId = receiverIdVO,
                        notiType = NotificationType.FRIEND_REQUEST,
                        title = FriendNotificationMessage.FriendRequest.TITLE,
                        message = FriendNotificationMessage.FriendRequest.message(requesterName),
                        refId = requesterId,
                        refType = RefType.USER,
                    ),
                )
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("친구 요청 알림 전송 실패 | receiverId=${receiverIdVO.value}", e)
            }
        }

        return friend
    }
}

private val LOGGER = AppLoggerFactory.createLogger<AddFriendUseCase>()
