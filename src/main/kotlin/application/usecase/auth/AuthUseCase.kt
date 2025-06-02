package com.peekr.application.usecase.auth

import com.peekr.application.dto.auth.LoginDto
import com.peekr.domain.model.entity.auth.JwtToken

interface AuthUseCase {
    suspend fun login(loginDto: LoginDto): JwtToken?

    suspend fun register(loginDto: LoginDto): JwtToken
}
