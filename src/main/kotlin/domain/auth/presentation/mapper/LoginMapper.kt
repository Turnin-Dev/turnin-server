package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.LoginResponse

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider,
    providerId = providerId,
    name = name,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)

fun JWTTokenDto.toResponse(): LoginResponse = LoginResponse(accessToken, refreshToken)
