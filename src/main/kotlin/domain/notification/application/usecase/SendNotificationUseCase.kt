package com.peekr.domain.notification.application.usecase

import com.peekr.common.firebase.FcmDataKey
import com.peekr.common.firebase.FcmMessage
import com.peekr.common.firebase.FcmService
import com.peekr.domain.notification.application.dto.NotificationDto
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.model.Notification
import com.peekr.domain.notification.domain.model.NotificationCommand
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import com.peekr.domain.notification.domain.repository.NotificationRepository

/**
 * 특정 사용자에게 알림 전송 및 저장
 *
 * @see invoke
 */
class SendNotificationUseCase(
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationRepository: NotificationRepository,
    private val fcmService: FcmService,
) {
    /**
     * 특정 사용자에게 알림을 전송하고 저장한다.
     *
     * @param command 알림 커맨드
     * @return 저장된 [Notification]
     */
    suspend operator fun invoke(command: NotificationCommand): NotificationDto {
        val userId = requireNotNull(command.userId) {
            "특정 사용자 알림 전송 시 userId는 필수입니다."
        }

        // 1. 활성 토큰 조회
        val tokens = fcmTokenRepository.findActiveTokens(userId)

        // 2. FCM 전송
        if (tokens.isNotEmpty()) {
            fcmService.sendToUsers(
                tokens = tokens,
                message = FcmMessage(
                    title = command.title ?: "",
                    body = command.message,
                    imageUrl = command.imageUrl,
                    notiType = command.notiType,
                    data = mapOf(
                        FcmDataKey.NOTI_TYPE to command.notiType.name,
                        FcmDataKey.REF_TYPE to (command.refType ?: ""),
                        FcmDataKey.REF_ID to (command.refId?.toString() ?: ""),
                    ),
                ),
            )
        }

        // 3. 알림 내역 저장 후 반환
        return notificationRepository.save(command).toDto()
    }
}
