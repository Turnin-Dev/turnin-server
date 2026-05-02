package com.turnin.domain.friend.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.suspendTransaction
import com.turnin.common.firebase.RefType
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId
import com.turnin.common.util.AppDispatchers
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.friend.domain.message.FriendNotificationMessage
import com.turnin.domain.friend.domain.model.FriendNotificationCommand
import com.turnin.domain.friend.domain.provider.NotificationProvider
import com.turnin.domain.friend.domain.repository.FriendRepository
import com.turnin.domain.friend.exception.FriendException
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
     * 역방향 요청(수신자가 이미 요청자에게 친구 요청을 보낸 상태)인 경우 자동으로 수락 처리한다.
     *
     * @param requesterId 친구 요청한 사용자 ID
     * @param receiverId 친구 요청받을 사용자 ID
     */
    suspend operator fun invoke(
        requesterId: Long,
        receiverId: Long,
    ) {
        val requesterIdVO = UserId(requesterId)
        val receiverIdVO = UserId(receiverId)

        // 1) 본인에게 친구 추가를 할 수 없다.
        if (requesterId == receiverId) {
            throw FriendException.SelfRequestException()
        }

        // 역방향 자동 수락 여부 (알림 분기에 사용)
        var isAutoAccepted = false

        val (notificationTargetId, requesterName) = suspendTransaction {
            // 2) 요청자/수신자 정보 조회 + 차단 관계 + 기존 친구 관계 확인
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

            // 4) 기존 친구 관계 처리
            val existingRelation = context.existingRelation
            if (existingRelation != null) {
                when {
                    // 4-1) 이미 친구 상태
                    existingRelation.status == FriendRequestStatus.ACCEPTED -> {
                        throw FriendException.AlreadyFriendException()
                    }

                    // 4-2) 동일 방향 중복 요청
                    !existingRelation.isReverse -> {
                        throw FriendException.AlreadyFriendRequestException()
                    }

                    // 4-3) 역방향 요청 존재 → 자동 수락 처리
                    else -> {
                        val accepted = friendRepository.updateFriendRequestStatus(
                            updaterId = requesterIdVO,
                            requesterId = receiverIdVO,
                            requestStatus = FriendRequestStatus.ACCEPTED,
                        )
                        if (!accepted) throw FriendException.UserNotFoundException()
                        isAutoAccepted = true
                        return@suspendTransaction receiverIdVO to context.requesterInfo.userName.value
                    }
                }
            }

            // 5) 신규 친구 요청 생성 (기존 관계 없는 경우에만 도달)
            try {
                friendRepository.createFriend(requesterIdVO, receiverIdVO)
            } catch (e: DatabaseException.DuplicatedDataException) {
                throw FriendException.AlreadyFriendRequestException(e)
            }

            receiverIdVO to context.requesterInfo.userName.value
        }

        // 6) 알림 전송 비동기 실행 (실패해도 친구 요청/수락은 성공으로 처리)
        applicationScope.launch(AppDispatchers.ioDispatcher) {
            runCatching {
                if (isAutoAccepted) {
                    // 6-1) 역방향 자동 수락: 원래 요청자(receiverId)에게 수락 알림 전송
                    notificationProvider.sendNotification(
                        FriendNotificationCommand(
                            userId = notificationTargetId,
                            notiType = NotificationType.FRIEND_ACCEPT,
                            title = FriendNotificationMessage.FriendAccept.TITLE,
                            message = FriendNotificationMessage.FriendAccept.message(requesterName),
                            refId = requesterId,
                            refType = RefType.USER,
                        ),
                    )
                } else {
                    // 6-2) 신규 친구 요청: 수신자(receiverId)에게 요청 알림 전송
                    notificationProvider.sendNotification(
                        FriendNotificationCommand(
                            userId = notificationTargetId,
                            notiType = NotificationType.FRIEND_REQUEST,
                            title = FriendNotificationMessage.FriendRequest.TITLE,
                            message = FriendNotificationMessage.FriendRequest.message(requesterName),
                            refId = requesterId,
                            refType = RefType.USER,
                        ),
                    )
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("친구 요청/자동수락 알림 전송 실패 | receiverId=${notificationTargetId.value}", e)
            }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<AddFriendUseCase>()
