package com.turnin.domain.auth.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.jwt.application.dto.toDto
import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.model.JWTTokenPayload
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.auth.application.dto.LoginDto
import com.turnin.domain.auth.application.dto.LoginResultDto
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import com.turnin.domain.auth.exception.AuthException

/**
 * 로그인 Usecase
 *
 * 소셜 로그인을 처리하고, 성공 시 사용자 ID와 JWT 토큰을 발급한다.
 */
class LoginUseCase(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) {
    /**
     * 소셜로그인
     *
     * @param loginDto [LoginDto]
     *
     * @return [LoginResultDto] 정상적으로 로그인이 진행된 경우
     * (로그인 실패 혹은 사용자를 가져올 수 없는 경우 **`null`** 반환)
     */
    suspend operator fun invoke(loginDto: LoginDto): LoginResultDto? {
        LOGGER.info(
            message = "Login attempt: provider=${loginDto.provider}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.LOGIN_ATTEMPT.value,
            ),
        )

        return try {
            val result = suspendTransaction {
                performLogin(loginDto)
            }

            LOGGER.info(
                message = "Login successful: userId=${result.userId.value}",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                    LogTag.ACTION.key to LogAction.LOGIN_SUCCESS.value,
                    LogTag.USER_ID.key to result.userId.value.toString(),
                ),
            )

            result
        } catch (e: AuthException) {
            LOGGER.debug("Login failed, ${e.message}")
            throw e
        } catch (e: TokenException) {
            LOGGER.debug("Login failed, ${e.message}")
            throw e
        } catch (e: Exception) {
            LOGGER.warn("Login process failed: ${e.message}")
            throw e
        }
    }

    // 로그인 수행
    private suspend fun performLogin(loginDto: LoginDto): LoginResultDto {
        // 1) 사용자 조회
        val authUser = getAuthUser(loginDto.provider, loginDto.providerId)
            ?: throw AuthException.UserNotFound()

        // 2) JWT 토큰 생성
        val jwtToken = generateJWTToken(
            userId = authUser.userId,
            displayId = authUser.displayId,
            role = authUser.role,
        )

        // 3) 마지막 로그인 일자 업데이트
        updateLastLoginAt(authUser.userId)

        // 4) 리프레쉬 토큰 저장
        val saved = saveRefreshToken(authUser.userId, jwtToken.refreshToken)
        if (!saved) {
            LOGGER.error(
                message = "RefreshToken save failed during login: userId=${authUser.userId.value}",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.TOKEN_SAVE_FAILURE.value,
                    LogTag.USER_ID.key to authUser.userId.value.toString(),
                ),
            )
            throw AuthException.RefreshTokenSaveFailed()
        }

        // 5) 결과 반환
        return LoginResultDto(authUser.userId, jwtToken.toDto())
    }

    // 사용자 조회
    private suspend fun getAuthUser(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? =
        authRepository.findAuthUserByProviderAndProviderId(provider, providerId)

    // JWT 토큰 생성
    private fun generateJWTToken(
        userId: UserId,
        displayId: DisplayId,
        role: Role,
    ): JWTToken {
        val payload = JWTTokenPayload(
            userId = userId.value.toString(),
            claims = mapOf(
                JWTClaimName.DISPLAY_ID to displayId.value,
                JWTClaimName.ROLE to role.name,
            ),
        )
        return jwtTokenService.generate(payload)
    }

    // 마지막 로그인 일자 업데이트
    private suspend fun updateLastLoginAt(userId: UserId) {
        authRepository.updateLastLoginAt(userId)
    }

    // 리프레쉬 토큰 저장
    private suspend fun saveRefreshToken(userId: UserId, refreshToken: String): Boolean =
        refreshTokenRepository.save(userId, refreshToken)
}

private val LOGGER = AppLoggerFactory.createLogger("LoginUseCase")
