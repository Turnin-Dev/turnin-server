package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.dto.RegisterResultDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService

/**
 * 회원가입
 */
class RegisterUseCase(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) {
    /**
     * @param registerDto [RegisterDto]
     *
     * @return [RegisterResultDto] 정상적으로 회원가입이 진행된 경우
     */
    suspend operator fun invoke(registerDto: RegisterDto): RegisterResultDto {
        LOGGER.debug("register called")
        val authUser = registerDto.toDomain()
        val registerResult = authService.register(authUser)
        val savedAuthUser = registerResult.authUser
        val jwtTokenDto = registerResult.jwtToken.toDto()
        refreshTokenService.save(savedAuthUser.userId, jwtTokenDto.refreshToken)
        LOGGER.debug("register successful, username: ${savedAuthUser.name}")
        return RegisterResultDto(savedAuthUser.userId, jwtTokenDto)
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RegisterUseCase")
