package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JwtTokenDto
import com.peekr.domain.auth.application.dto.LoginDto

interface AuthUseCase {
    suspend fun login(loginDto: LoginDto): JwtTokenDto?

    suspend fun register(loginDto: LoginDto): JwtTokenDto
}
