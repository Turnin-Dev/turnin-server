package com.peekr.presentation.mapper.auth

import com.peekr.application.dto.auth.JwtTokenDto
import com.peekr.application.dto.auth.LoginDto
import com.peekr.presentation.dto.auth.LoginRequest
import com.peekr.presentation.dto.auth.LoginResponse

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider,
    providerId = providerId,
    name = name,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)

fun JwtTokenDto.toResponse(): LoginResponse = LoginResponse(accessToken, refreshToken)
