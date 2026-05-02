package com.turnin.domain.friend.application.usecase

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 친구 상태 수정
 */
class UpdateFriendRequestStatusUseCase(
    private val friendRepository: FriendRepository,
    private val notificationProvider: NotificationProvider,
    private val applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = AppDispatchers.ioDispatcher,
) {
    /**
     * 친구 상태를 수정한다.
     *
     * 현재 친구 수락([FriendRequestStatus.ACCEPTED]) 케이스만 지원한다.
     * 친구 요청 수락 시 원래 요청을 보낸 사람(requesterId)에게 수락 알림을 전송한다.
     * 알림 전송 실패 시에도 상태 수정은 성공으로 처리한다.
     *
     * @param updaterId 상태 수정 주체자 ID (수락하는 사람, 원래 수신자)
     * @param requesterId 원래 친구 요청을 보낸 사람 ID
     * @param requestStatus 요청 상태 ([FriendRequestStatus])
     */
    suspend operator fun invoke(
        updaterId: Long,
        requesterId: Long,
        requestStatus: FriendRequestStatus,
    ) {
        val updaterIdVO = UserId(updaterId)
        val requesterIdVO = UserId(requesterId)

        // +) 현재 지원하는 상태 수정은 수락(ACCEPTED) 뿐이다.
        if (requestStatus != FriendRequestStatus.ACCEPTED) throw FriendException.UnsupportedRequestStatusException()

        // 1) 스스로 친구 관계가 될 수 없다.
        if (updaterId == requesterId) throw FriendException.SelfRequestException()

        val requesterName = suspendTransaction {
            // 2) 수정 주체자/요청자 정보 조회 + 차단 관계 + 기존 친구 관계 확인
            val context = friendRepository.getFriendRequestContext(updaterIdVO, requesterIdVO)
                ?: throw FriendException.UserNotFoundException()

            // 3) 차단 관계인 경우 (보안상 사용자를 찾을 수 없다는 예외로 처리)
            if (context.isBlocked) {
                LOGGER.warn(
                    "User already filtered by visibility check, but reached friend request logic" +
                        "(Updater ID: ${updaterIdVO.value}, Requester ID: ${requesterIdVO.value})",
                )
                throw FriendException.UserNotFoundException()
            }

            // 4) 기존 친구 관계 확인
            val existingRelation = context.existingRelation

            when {
                // 4-1) 수락할 요청이 없는 경우
                existingRelation == null -> {
                    throw FriendException.FriendRequestNotFoundException()
                }

                // 4-2) 이미 친구 상태인 경우
                existingRelation.status == FriendRequestStatus.ACCEPTED -> {
                    throw FriendException.AlreadyFriendException()
                }
            }

            // 5) 친구 상태 수정
            val result = friendRepository.updateFriendRequestStatus(
                updaterId = updaterIdVO,
                requesterId = requesterIdVO,
                requestStatus = requestStatus,
            )
            if (!result) throw FriendException.FriendRequestNotFoundException()

            context.requesterInfo.userName.value
        }

        // 6) 친구 수락 시 원래 요청을 보낸 사람(requesterId)에게 수락 알림 전송 비동기 실행 (실패해도 상태 수정은 성공으로 처리)
        applicationScope.launch(ioDispatcher) {
            runCatching {
                notificationProvider.sendNotification(
                    FriendNotificationCommand(
                        userId = requesterIdVO,
                        notiType = NotificationType.FRIEND_ACCEPT,
                        title = FriendNotificationMessage.FriendAccept.TITLE,
                        message = FriendNotificationMessage.FriendAccept.message(requesterName),
                        refId = updaterId,
                        refType = RefType.USER,
                    ),
                )
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("친구 수락 알림 전송 실패 | requesterId=${requesterIdVO.value}", e)
            }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<UpdateFriendRequestStatusUseCase>()
