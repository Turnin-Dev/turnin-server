package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService

class AuthUseCaseImpl(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JWTTokenDto? {
        val loginResult = authService.login(loginDto.provider, loginDto.providerId)
        if (loginResult == null) return null
        saveRefreshToken(loginResult.authUser.id, loginResult.jwtToken.refreshToken)
        return loginResult.jwtToken.toDto()
    }

    override suspend fun register(registerDto: RegisterDto): JWTTokenDto {
        val authUser = registerDto.toDomain()
        val registerResult = authService.register(authUser)
        val savedAuthUser = registerResult.authUser
        val jwtTokenDto = registerResult.jwtToken.toDto()
        saveRefreshToken(savedAuthUser.id, jwtTokenDto.refreshToken)
        return jwtTokenDto
    }

    override suspend fun refresh(userId: Long, token: String): JWTTokenDto? {
        val newToken = authService.refresh(token)
        return newToken?.let {
            saveRefreshToken(userId, it.refreshToken)
            newToken.toDto()
        }
    }

    private suspend fun saveRefreshToken(userId: Long, token: String) {
        refreshTokenService.save(userId, token)
    }

    override suspend fun extractUserId(token: String): String? =
        refreshTokenService.extractUserId(token)
}
