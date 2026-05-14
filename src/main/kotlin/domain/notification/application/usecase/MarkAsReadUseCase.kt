package com.turnin.domain.notification.application.usecase

import com.turnin.common.model.id.NotificationId
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.domain.repository.NotificationRepository

/**
 * 특정 알림 읽음 처리
 *
 * @see invoke
 */
class MarkAsReadUseCase(private val notificationRepository: NotificationRepository) {
    /**
     * 특정 알림을 읽음 처리한다.
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param userId 요청한 사용자 ID
     * @return 읽음 처리 성공 시 true, 알림을 찾지 못한 경우 false
     */
    suspend operator fun invoke(notificationId: Long, userId: UserId): Boolean {
        val notificationIdVO = NotificationId(notificationId)
        return notificationRepository.markAsRead(notificationIdVO, userId)
    }
}
