package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository

/**
 * 모든 FCM 토큰 비활성화
 *
 * @see invoke
 */
class DeactivateAllFcmTokensUseCase(private val fcmTokenRepository: FcmTokenRepository) {
    /**
     * 모든 FCM 토큰을 비활성화한다. (모든 기기 로그아웃 시 호출)
     *
     * @param userId 토큰을 비활성화할 사용자 ID
     */
    suspend operator fun invoke(userId: UserId) =
        fcmTokenRepository.deactivateAll(userId)
}
