package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.application.mapper.AuthMapper.toDto
import com.peekr.domain.auth.domain.service.AuthService

class AuthUseCaseImpl(private val authService: AuthService) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JWTTokenDto? {
        val jwtToken = authService.login(loginDto.provider, loginDto.providerId)
        return jwtToken.toDto()
    }

    override suspend fun register(registerDto: RegisterDto): JWTTokenDto {
        val authUser = registerDto.toDomain()
        val jwtToken = authService.register(authUser)
        return jwtToken.toDto()!!
    }
}
