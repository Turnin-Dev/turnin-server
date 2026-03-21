package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository

/**
 * 모든 FCM 토큰 삭제
 *
 * @see invoke
 */
class DeleteAllFcmTokensUseCase(private val fcmTokenRepository: FcmTokenRepository) {
    /**
     * 모든 FCM 토큰을 삭제한다. (회원 탈퇴 시 호출)
     *
     * @param userId 토큰을 삭제할 사용자 ID
     */
    suspend operator fun invoke(userId: UserId) =
        fcmTokenRepository.deleteAll(userId)
}
