package com.peekr.application.usecase.auth

import com.peekr.application.dto.auth.UserDto
import com.peekr.domain.model.entity.auth.JwtToken

interface AuthUseCase {
    suspend fun login(userDto: UserDto): JwtToken?

    suspend fun register(userDto: UserDto): JwtToken
}
