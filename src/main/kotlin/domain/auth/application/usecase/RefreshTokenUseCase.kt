package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService

/**
 * 리프레쉬 토큰 갱신
 */
class RefreshTokenUseCase(
    private val jwtTokenService: JWTTokenService,
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) {
    /**
     * @param token 리프레쉬 토큰
     *
     * @return [JWTTokenDto] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    suspend operator fun invoke(token: String): JWTTokenDto? {
        val subject = jwtTokenService.extractSubjectWithToken(token, JWTTokenType.Refresh)?.toLongOrNull()
        if (subject == null) {
            LOGGER.debug("refresh is Null, token: ${token.masking()}")
            return null
        }
        val userId = UserId(subject)
        LOGGER.debug("refresh called, userId: $userId, token: ${token.masking()}")
        val newToken = authService.refresh(token)
        if (newToken == null) {
            LOGGER.debug("refresh failed, userId: $userId, token: ${token.masking()}")
            return null
        }
        LOGGER.debug("refresh successful")
        refreshTokenService.save(userId, newToken.refreshToken)
        return newToken.toDto()
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RefreshTokenUseCase")
