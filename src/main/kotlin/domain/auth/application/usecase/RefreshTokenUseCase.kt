package com.peekr.domain.auth.application.usecase

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserIdValidationException
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.LogAction
import com.peekr.common.util.LogTag
import com.peekr.common.util.LogType
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository

class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) {
    /**
     * 리프레쉬 토큰 갱신
     *
     * @param token 리프레쉬 토큰
     *
     * @throws UserIdValidationException UserId 유효성 검사 실패 시
     *
     * @return [JWTTokenDto] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    suspend operator fun invoke(token: String): JWTTokenDto? {
        // 토큰에서 Subject(사용자 ID) 추출
        val subject = jwtTokenService.extractSubjectWithToken(token, JWTTokenType.Refresh)?.toLongOrNull()
        if (subject == null) {
            LOGGER.warn(
                message = "Token refresh failed: Invalid subject in token",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.TOKEN_REFRESH_FAILURE.value,
                ),
            )
            return null
        }
        val userId = UserId(subject)

        // 리프레쉬 토큰 갱신 진행
        LOGGER.info(
            message = "Token refresh attempt: userId=${userId.value}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.TOKEN_REFRESH_ATTEMPT.value,
                LogTag.USER_ID.key to userId.value.toString(),
            ),
        )
        val newToken = authRefresh(token)
        if (newToken == null) {
            LOGGER.warn(
                message = "Token refresh failed: userId=${userId.value}",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                    LogTag.ACTION.key to LogAction.TOKEN_REFRESH_FAILURE.value,
                    LogTag.USER_ID.key to userId.value.toString(),
                ),
            )
            return null
        }

        // (갱신 성공) 리프레쉬 토큰 저장 후 반환
        LOGGER.debug("refresh successful")
        refreshTokenRepository.save(userId, newToken.refreshToken)

        LOGGER.info(
            message = "Token refresh successful: userId=${userId.value}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.TOKEN_REFRESH_SUCCESS.value,
                LogTag.USER_ID.key to userId.value.toString(),
            ),
        )

        return newToken.toDto()
    }

    /**
     * 리프레쉬 토큰 갱신
     *
     * @param token 리프레쉬 토큰
     *
     * @return [JWTToken] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    private suspend fun authRefresh(token: String): JWTToken? {
        return try {
            // 1) 서명/만료 검증 실패 시 즉시 종료
            verifyRefreshToken(token) ?: return null

            // 2) 저장소 확인
            val userId = refreshTokenRepository.findUserIdByRefreshToken(token) ?: return null
            val authUser = authRepository.findUserByUserId(userId) ?: return null

            // 3) 액세스 토큰 재발급
            val payload = JWTTokenPayload(
                userId = authUser.userId.value.toString(),
                claimName = JWTClaimName.DISPLAY_ID,
                claim = authUser.displayId.value,
            )
            jwtTokenService.generate(payload)
        } catch (e: Exception) {
            LOGGER.error(e, "Unexpected error during authRefresh")
            null
        }
    }

    // 리프레쉬 토큰 검증
    private fun verifyRefreshToken(token: String): DecodedJWT? = try {
        val verifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)
        verifier.verify(token)
    } catch (e: Exception) {
        LOGGER.debug("JWT verification failed: ${e.message}")
        null
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RefreshTokenUseCase")
