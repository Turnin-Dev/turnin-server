package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.application.dto.FcmTokenDto
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.model.FcmToken
import com.peekr.domain.notification.domain.repository.FcmTokenRepository

/**
 * FCM 토큰 등록
 *
 * @see invoke
 */
class RegisterFcmTokenUseCase(private val fcmTokenRepository: FcmTokenRepository) {
    /**
     * FCM 토큰을 등록한다. (로그인 / 회원가입 시 호출)
     *
     * @param userId 토큰을 등록할 사용자 ID
     * @param token FCM 토큰
     * @return 등록된 [FcmToken]
     */
    suspend operator fun invoke(userId: UserId, token: String): FcmTokenDto =
        fcmTokenRepository.upsert(userId, token).toDto()
}
