package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.value.toSocialLoginProvider
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider.toSocialLoginProvider(),
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    provider = provider,
    providerId = providerId,
    name = name,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)
