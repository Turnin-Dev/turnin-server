package com.peekr.application.usecase.auth

import com.peekr.application.dto.auth.LoginDto
import com.peekr.application.mapper.auth.AuthMapper.toDomain
import com.peekr.domain.model.entity.auth.JwtToken
import com.peekr.domain.service.auth.AuthService

class AuthUseCaseImpl(
    private val authService: AuthService,
) : AuthUseCase {
    override suspend fun login(loginDto: LoginDto): JwtToken? {
        val authUser = loginDto.toDomain()
        return authService.login(authUser.provider, authUser.providerId)
    }

    override suspend fun register(loginDto: LoginDto): JwtToken {
        val authUser = loginDto.toDomain()
        return authService.register(authUser)
    }
}
