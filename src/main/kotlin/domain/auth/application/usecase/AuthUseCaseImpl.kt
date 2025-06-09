package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.application.mapper.AuthMapper.toDto
import com.peekr.domain.auth.domain.service.AuthService

class AuthUseCaseImpl(
    private val authService: AuthService,
) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JWTTokenDto? {
        val authUser = loginDto.toDomain()
        val jwtToken = authService.login(authUser.provider, authUser.providerId)
        return jwtToken.toDto()
    }

    override suspend fun register(loginDto: LoginDto): JWTTokenDto {
        val authUser = loginDto.toDomain()
        val jwtToken = authService.register(authUser)
        return jwtToken.toDto()!!
    }
}
