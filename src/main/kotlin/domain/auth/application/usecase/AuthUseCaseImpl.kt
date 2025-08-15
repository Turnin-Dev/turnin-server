package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.application.mapper.AuthMapper.toDto
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService

class AuthUseCaseImpl(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JWTTokenDto? {
        LOGGER.debug("login called: $loginDto")
        val loginResult = authService.login(loginDto.provider, loginDto.providerId)
        if (loginResult == null) {
            LOGGER.debug(
                "login failed, provider: ${loginDto.provider}, providerId: ${loginDto.providerId.masking()}",
            )
            return null
        }
        saveRefreshToken(loginResult.authUser.id, loginResult.jwtToken.refreshToken)
        LOGGER.debug("login successful")
        return loginResult.jwtToken.toDto()
    }

    override suspend fun register(registerDto: RegisterDto): JWTTokenDto {
        LOGGER.debug("register called")
        val authUser = registerDto.toDomain()
        val registerResult = authService.register(authUser)
        val savedAuthUser = registerResult.authUser
        val jwtTokenDto = registerResult.jwtToken.toDto()
        saveRefreshToken(savedAuthUser.id, jwtTokenDto.refreshToken)
        LOGGER.debug("register successful, username: ${savedAuthUser.name}")
        return jwtTokenDto
    }

    override suspend fun refresh(userId: Long, token: String): JWTTokenDto? {
        val newToken = authService.refresh(token)
        return newToken?.let {
            val extractedUserId = extractUserId(token) ?: userId
            saveRefreshToken(extractedUserId, it.refreshToken)
            newToken.toDto()
        }
    }

    override suspend fun findUser(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): FindUserResultDto {
        val findUserResult = authService.findUser(provider, providerId)
        return findUserResult.toDto()
    }

    private suspend fun saveRefreshToken(userId: Long, token: String) {
        refreshTokenService.save(userId, token)
    }

    override suspend fun extractUserId(token: String): Long? =
        refreshTokenService.extractUserId(token)
}

private val LOGGER = AppLoggerFactory.createLogger("AuthUseCaseImpl")
