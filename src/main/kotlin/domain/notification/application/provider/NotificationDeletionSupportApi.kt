package com.peekr.domain.notification.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import com.peekr.domain.notification.domain.repository.NotificationRepository

/**
 * 외부에 제공할 Notification 삭제 제공 API
 */
class NotificationDeletionSupportApi(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenRepository: FcmTokenRepository,
) {
    /**
     * 해당 사용자의 모든 FCM 토큰과 모든 알림을 삭제한다.
     * (회원 탈퇴 시 호출)
     *
     * @param userId 알림 내역과 FCM 토큰을 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId) {
        fcmTokenRepository.deleteAll(userId)
        notificationRepository.deleteAll(userId)
    }
}
