package com.turnin.domain.notification.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.firebase.FcmDataKey
import com.turnin.common.firebase.FcmMessage
import com.turnin.common.firebase.FcmService
import com.turnin.domain.notification.application.dto.NotificationDto
import com.turnin.domain.notification.application.dto.toDto
import com.turnin.domain.notification.domain.model.Notification
import com.turnin.domain.notification.domain.model.NotificationCommand
import com.turnin.domain.notification.domain.repository.FcmTokenRepository
import com.turnin.domain.notification.domain.repository.NotificationRepository
import com.turnin.domain.notification.exception.NotificationException

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
        val userId = command.userId
            ?: throw NotificationException.MissingUserIdInPersonalNotification()

        val (notification, tokens) = suspendTransaction {
            // 1. 알림 내역 먼저 저장
            // 주의: FCM 전송 실패 후 재시도 시 중복 저장 가능성 있음.
            // 추후 트래픽 증가 시 Outbox 패턴 또는 멱등 키 기반 중복 방지 도입 고려.
            val notification = notificationRepository.save(command)

            // 2. 활성 토큰 조회
            val tokens = fcmTokenRepository.findActiveTokens(userId)

            notification to tokens
        }

        // 3. FCM 전송
        if (tokens.isNotEmpty()) {
            fcmService.sendToUsers(
                tokens = tokens,
                message = FcmMessage(
                    title = command.title ?: "",
                    body = command.message,
                    imageUrl = command.imageUrl,
                    notiType = command.notiType,
                    data = buildMap {
                        command.refType?.let { put(FcmDataKey.REF_TYPE, it) }
                        command.refId?.let { put(FcmDataKey.REF_ID, it.toString()) }
                    },
                ),
            )
        }

        return notification.toDto()
    }
}
