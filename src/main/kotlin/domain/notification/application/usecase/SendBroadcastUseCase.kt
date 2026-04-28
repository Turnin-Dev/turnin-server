package com.turnin.domain.notification.application.usecase

import com.turnin.common.firebase.FcmMessage
import com.turnin.common.firebase.FcmService
import com.turnin.common.firebase.FcmTopic
import com.turnin.domain.notification.application.dto.NotificationDto
import com.turnin.domain.notification.application.dto.toDto
import com.turnin.domain.notification.domain.model.Notification
import com.turnin.domain.notification.domain.model.NotificationCommand
import com.turnin.domain.notification.domain.repository.NotificationRepository

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
        // 주의: FCM 전송 실패 후 재시도 시 중복 저장 가능성 있음.
        // 브로드캐스트 알림은 관리자가 수동으로 발송하는 구조라
        // 자동 재시도가 없으므로 현재 단계에서는 허용 가능한 수준.
        // 추후 트래픽 증가 시 Outbox 패턴 도입 고려.
        val notification = notificationRepository.save(command)

        // 2. FCM Topic 전송
        fcmService.sendToTopic(
            FcmMessage(
                topic = FcmTopic.ALL,
                title = command.title ?: "",
                body = command.message,
                notiType = command.notiType,
            ),
        )

        return notification.toDto()
    }
}
