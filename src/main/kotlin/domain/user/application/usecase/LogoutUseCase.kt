package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.provider.AuthProvider

/**
 * 로그아웃
 *
 * @see invoke
 */
class LogoutUseCase(private val authProvider: AuthProvider) {
    /**
     * 로그아웃을 수행한다.
     *
     * 자세한 내용은 기능 명세서 **`RQ-2`** 참고
     *
     * @param userId 사용자 ID
     */
    suspend operator fun invoke(userId: Long) {
        val userIDVO = UserId(userId)

        // 1. 토큰 삭제
        authProvider.deleteRefreshToken(userIDVO)
        // 2. 기기 정보 해제 (사용자와 매핑된 FCM 토큰 정보 제거)
    }
}
