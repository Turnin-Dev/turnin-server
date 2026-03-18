package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository

/**
 * 특정 FCM 토큰 비활성화
 *
 * @see invoke
 */
class DeactivateFcmTokenUseCase(private val fcmTokenRepository: FcmTokenRepository) {
    /**
     * 특정 FCM 토큰을 비활성화한다. (로그아웃 시 호출)
     *
     * @param userId 토큰 소유자 ID
     * @param token 비활성화할 FCM 토큰
     * @return 비활성화 성공 시 true, 토큰을 찾지 못한 경우 false
     */
    suspend operator fun invoke(userId: UserId, token: String): Boolean =
        fcmTokenRepository.deactivate(userId, token)
}
