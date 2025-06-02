package com.peekr.application.usecase.auth

import com.peekr.application.dto.auth.JwtTokenDto
import com.peekr.application.dto.auth.LoginDto

interface AuthUseCase {
    suspend fun login(loginDto: LoginDto): JwtTokenDto?

    suspend fun register(loginDto: LoginDto): JwtTokenDto
}
