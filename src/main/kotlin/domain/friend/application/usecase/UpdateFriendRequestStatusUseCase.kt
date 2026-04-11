package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.firebase.RefType
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppDispatchers
import com.peekr.common.util.log.AppLoggerFactory
import com.peekr.domain.friend.domain.message.FriendNotificationMessage
import com.peekr.domain.friend.domain.model.FriendNotificationCommand
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 친구 상태 수정
 */
class UpdateFriendRequestStatusUseCase(
    private val friendRepository: FriendRepository,
    private val notificationProvider: NotificationProvider,
    private val applicationScope: CoroutineScope,
) {
    /**
     * 친구 상태를 수정한다.
     *
     * 친구 요청 수락 시([FriendRequestStatus.ACCEPTED]) 수신자에게 수락 알림을 전송한다.
     * 알림 전송 실패 시에도 상태 수정은 성공으로 처리한다.
     *
     * @param requesterId 상태 수정을 요청하는 사용자 ID (수락/거절하는 사람)
     * @param receiverId 상태 수정 요청을 받는 사용자 ID (원래 친구 요청을 보낸 사람)
     * @param requestStatus 요청 상태 ([FriendRequestStatus])
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend operator fun invoke(
        requesterId: Long,
        receiverId: Long,
        requestStatus: FriendRequestStatus,
    ): Boolean {
        val requesterIdVO = UserId(requesterId)
        val receiverIdVO = UserId(receiverId)

        // 1) 스스로 친구 관계가 될 수 없다.
        if (requesterId == receiverId) throw FriendException.SelfRequestException()

        val (result, requesterName) = suspendTransaction {
            // 2) 요청자/수신자 정보 조회 + 존재 여부 확인
            val context = friendRepository.getFriendRequestContext(requesterIdVO, receiverIdVO)
                ?: throw FriendException.UserNotFoundException()

            // 3) 친구 상태 수정
            val result = friendRepository.updateFriendRequestStatus(requesterIdVO, receiverIdVO, requestStatus)

            result to context.requesterInfo.userName.value
        }

        // 4) 친구 수락 시 수신자(원래 친구 요청을 보낸 사람)에게 알림 전송 비동기 실행 (실패해도 상태 수정은 성공으로 처리)
        if (result && requestStatus == FriendRequestStatus.ACCEPTED) {
            applicationScope.launch(AppDispatchers.ioDispatcher) {
                runCatching {
                    notificationProvider.sendNotification(
                        FriendNotificationCommand(
                            userId = receiverIdVO,
                            notiType = NotificationType.FRIEND_ACCEPT,
                            title = FriendNotificationMessage.FriendAccept.TITLE,
                            message = FriendNotificationMessage.FriendAccept.message(requesterName),
                            refId = requesterId,
                            refType = RefType.USER,
                        ),
                    )
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    LOGGER.warn("친구 수락 알림 전송 실패 | receiverId=${receiverIdVO.value}", e)
                }
            }
        }

        return result
    }
}

private val LOGGER = AppLoggerFactory.createLogger<UpdateFriendRequestStatusUseCase>()
