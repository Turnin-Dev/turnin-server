package com.peekr.domain.auth.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.LoginResultDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException

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
        LOGGER.debug("login called, provider: ${loginDto.provider}, providerId: ${loginDto.providerId.masking()}")
        return try {
            val result = suspendTransaction {
                performLogin(loginDto)
            }
            LOGGER.debug("login successful, userId: ${result?.userId}")
            result
        } catch (e: AuthException) {
            LOGGER.debug("login failed, ${e.message}")
            throw e
        } catch (e: TokenException) {
            LOGGER.debug("login failed, ${e.message}")
            throw e
        } catch (e: Exception) {
            LOGGER.debug("unexpected error during login", e)
            null
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
        )

        // 3) 마지막 로그인 일자 업데이트
        updateLastLoginAt(authUser.userId)

        // 4) 리프레쉬 토큰 저장
        val saved = saveRefreshToken(authUser.userId, jwtToken.refreshToken)
        if (!saved) {
            LOGGER.debug("token refresh failed, userId: ${authUser.userId}")
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
    ): JWTToken {
        val payload = JWTTokenPayload(
            userId = userId.value.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = displayId.value,
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
