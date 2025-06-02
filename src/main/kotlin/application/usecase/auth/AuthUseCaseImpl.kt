package com.peekr.application.usecase.auth

import com.peekr.application.dto.auth.JwtTokenDto
import com.peekr.application.dto.auth.LoginDto
import com.peekr.application.mapper.auth.AuthMapper.toDomain
import com.peekr.application.mapper.auth.AuthMapper.toDto
import com.peekr.domain.service.auth.AuthService

class AuthUseCaseImpl(
    private val authService: AuthService,
) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JwtTokenDto? {
        val authUser = loginDto.toDomain()
        val jwtToken = authService.login(authUser.provider, authUser.providerId)
        return jwtToken.toDto()
    }

    override suspend fun register(loginDto: LoginDto): JwtTokenDto {
        val authUser = loginDto.toDomain()
        val jwtToken = authService.register(authUser)
        return jwtToken.toDto()!!
    }
}
