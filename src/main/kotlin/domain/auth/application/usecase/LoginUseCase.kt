package com.peekr.domain.auth.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.LoginResultDto
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) {
    /**
     * 소셜로그인
     *
     * 로그인에 성공 시 리프레쉬 토큰을 저장한다.
     *
     * @param loginDto [LoginDto]
     *
     * @return [LoginResultDto] 정상적으로 로그인이 진행된 경우
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    suspend operator fun invoke(loginDto: LoginDto): LoginResultDto? = suspendTransaction {
        // 소셜 로그인 진행
        LOGGER.debug(
            "login called, provider: ${loginDto.provider}, providerId: ${loginDto.providerId.masking()}",
        )
        val loginResult = authLogin(loginDto.provider, loginDto.providerId)
        if (loginResult == null) {
            LOGGER.debug(
                "login failed, provider: ${loginDto.provider}, providerId: ${loginDto.providerId.masking()}",
            )
            return@suspendTransaction null
        }

        // 리프레쉬 토큰 저장
        val saved = refreshTokenRepository.save(loginResult.authUser.userId, loginResult.jwtToken.refreshToken)
        if (!saved) {
            LOGGER.debug("token refresh failed, userId: ${loginResult.authUser.userId}")
            return@suspendTransaction null
        }

        // (로그인 성공) 토큰 변환 후 결과 반환
        LOGGER.debug("login successful")
        val jwtTokenDto = loginResult.jwtToken.toDto()
        LoginResultDto(loginResult.authUser.userId, jwtTokenDto)
    }

    /**
     * 소셜로그인
     *
     * [provider]와 [providerId]로 유저를 조회하고 로그인을 진행한다.
     * 만약, 계정이 존재하지 않는다면 null을 반환한다.
     *
     * @return [LoginResult] 정상적으로 로그인이 진행된 경우 로그인 결과 값 반환
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    private suspend fun authLogin(
        provider: SocialLoginProvider,
        providerId: String,
    ): LoginResult? {
        val authUser = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)
        if (authUser == null) return null

        val payload = JWTTokenPayload(
            userId = authUser.userId.value.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = authUser.displayId.value,
        )
        val jwtToken = jwtTokenService.generate(payload)
        val loginResult = LoginResult(jwtToken, authUser)

        authRepository.updateLastLoginAt(authUser.userId)
        return loginResult
    }
}

private val LOGGER = AppLoggerFactory.createLogger("LoginUseCase")
