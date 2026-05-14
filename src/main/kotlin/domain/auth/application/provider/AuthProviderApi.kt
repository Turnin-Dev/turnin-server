package com.turnin.domain.auth.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository

/**
 * 외부에 제공할 Auth API
 */
class AuthProviderApi(private val refreshTokenRepository: RefreshTokenRepository) {
    /**
     * 리프레쉬 토큰을 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteRefreshToken(userId: UserId): Unit =
        refreshTokenRepository.delete(userId)
}
