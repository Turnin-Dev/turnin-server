package com.peekr.domain.auth.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.LoginResultDto
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService

/**
 * 소셜로그인
 *
 * 로그인에 성공 시 리프레쉬 토큰을 저장한다.
 */
class LoginUseCase(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) {
    /**
     * @param loginDto [LoginDto]
     *
     * @return [LoginResultDto] 정상적으로 로그인이 진행된 경우
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    suspend operator fun invoke(loginDto: LoginDto): LoginResultDto? = suspendTransaction {
        LOGGER.debug("login called: $loginDto")
        val loginResult = authService.login(loginDto.provider, loginDto.providerId)
        if (loginResult == null) {
            LOGGER.debug(
                "login failed, provider: ${loginDto.provider}, providerId: ${loginDto.providerId.masking()}",
            )
            return@suspendTransaction null
        }
        refreshTokenService.save(loginResult.authUser.userId, loginResult.jwtToken.refreshToken)
        LOGGER.debug("login successful")
        val jwtTokenDto = loginResult.jwtToken.toDto()
        LoginResultDto(loginResult.authUser.userId, jwtTokenDto)
    }
}

private val LOGGER = AppLoggerFactory.createLogger("LoginUseCase")
