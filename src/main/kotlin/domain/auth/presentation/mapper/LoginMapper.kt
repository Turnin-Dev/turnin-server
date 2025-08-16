package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProviderForAuth
import com.peekr.domain.auth.presentation.dto.FindUserResultResponse
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)

fun FindUserResultDto.toResponse(): FindUserResultResponse = FindUserResultResponse(exists)
