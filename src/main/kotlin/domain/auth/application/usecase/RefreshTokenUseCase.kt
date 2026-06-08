package com.turnin.domain.auth.application.usecase

import com.auth0.jwt.interfaces.DecodedJWT
import com.turnin.common.jwt.application.dto.JWTTokenDto
import com.turnin.common.jwt.application.dto.toDto
import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.model.JWTTokenPayload
import com.turnin.common.jwt.domain.model.JWTTokenType
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserIdValidationException
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogLevel
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository

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
        try {
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
            val saved = refreshTokenRepository.save(userId, newToken.refreshToken)
            if (!saved) {
                LOGGER.warn(
                    message = "Token refresh failed during token rotation: userId=${userId.value}",
                    tags = mapOf(
                        LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                        LogTag.ACTION.key to LogAction.TOKEN_SAVE_FAILURE.value,
                        LogTag.USER_ID.key to userId.value.toString(),
                    ),
                )
                return null
            }

            LOGGER.info(
                message = "Token refresh successful: userId=${userId.value}",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                    LogTag.ACTION.key to LogAction.TOKEN_REFRESH_SUCCESS.value,
                    LogTag.USER_ID.key to userId.value.toString(),
                ),
            )

            return newToken.toDto()
        } catch (e: TokenException) {
            val tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                LogTag.ACTION.key to LogAction.TOKEN_REFRESH_FAILURE.value,
            )
            val message = "Token refresh failed: ${e.message}"
            when (e.logLevel) {
                LogLevel.INFO -> LOGGER.info(message = message, tags = tags, e = e)
                LogLevel.WARN -> LOGGER.warn(message = message, tags = tags, e = e)
                LogLevel.ERROR -> LOGGER.error(message = message, tags = tags, e = e)
                LogLevel.DEBUG -> LOGGER.debug(message = message, tags = tags, e = e)
            }
            return null
        }
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
        // 1) 토큰 검증 실패 시 즉시 종료
        verifyRefreshToken(token) ?: return null

        // 2) 저장소 확인
        val userId = refreshTokenRepository.findUserIdByRefreshToken(token) ?: return null
        val authUser = authRepository.findUserByUserId(userId) ?: return null

        // 3) 액세스 토큰 재발급
        val payload = JWTTokenPayload(
            userId = authUser.userId.value.toString(),
            claims = mapOf(
                JWTClaimName.DISPLAY_ID to authUser.displayId.value,
                JWTClaimName.ROLE to authUser.role.name,
            ),
        )

        return try {
            jwtTokenService.generate(payload)
        } catch (e: TokenException.CannotCreateToken) {
            LOGGER.error(
                message = "Token refresh failed: token generation error",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.TOKEN_REFRESH_FAILURE.value,
                ),
                e = e,
            )
            null
        }
    }

    // 리프레쉬 토큰 검증
    // 만료는 정상적인 흐름으로 처리 (null 반환), 그 외 예외는 상위로 전파
    private fun verifyRefreshToken(token: String): DecodedJWT? = try {
        jwtTokenService.verify(token, JWTTokenType.Refresh)
    } catch (e: TokenException.TokenExpiredException) {
        LOGGER.debug(message = e.message, e = e)
        null
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RefreshTokenUseCase")
