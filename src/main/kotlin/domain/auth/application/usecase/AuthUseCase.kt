package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto

interface AuthUseCase {
    suspend fun login(loginDto: LoginDto): JWTTokenDto?

    suspend fun register(registerDto: RegisterDto): JWTTokenDto
}
