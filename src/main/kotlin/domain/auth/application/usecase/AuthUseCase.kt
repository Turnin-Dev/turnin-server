package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto

interface AuthUseCase {
    suspend fun login(loginDto: LoginDto): JWTTokenDto?

    suspend fun register(loginDto: LoginDto): JWTTokenDto
}
