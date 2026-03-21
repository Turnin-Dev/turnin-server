package com.peekr.domain.notification.application.usecase

import com.peekr.common.firebase.FcmDataKey
import com.peekr.common.firebase.FcmMessage
import com.peekr.common.firebase.FcmService
import com.peekr.common.firebase.FcmTopic
import com.peekr.domain.notification.application.dto.NotificationDto
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.model.Notification
import com.peekr.domain.notification.domain.model.NotificationCommand
import com.peekr.domain.notification.domain.repository.NotificationRepository

/**
 * 전체 사용자에게 브로드캐스트 알림 전송 및 저장
 *
 * @see invoke
 */
class SendBroadcastUseCase(
    private val notificationRepository: NotificationRepository,
    private val fcmService: FcmService,
) {
    /**
     * 전체 사용자에게 브로드캐스트 알림을 전송하고 저장한다.
     *
     * @param command 알림 커맨드
     * @return 저장된 [Notification]
     */
    suspend operator fun invoke(command: NotificationCommand): NotificationDto {
        // 1. 브로드캐스트 알림 먼저 저장
        val notification = notificationRepository.save(command)

        // 2. FCM Topic 전송
        fcmService.sendToTopic(
            FcmMessage(
                topic = FcmTopic.ALL,
                title = command.title ?: "",
                body = command.message,
                notiType = command.notiType,
                data = mapOf(
                    FcmDataKey.NOTI_TYPE to command.notiType.name,
                ),
            ),
        )

        return notification.toDto()
    }
}
