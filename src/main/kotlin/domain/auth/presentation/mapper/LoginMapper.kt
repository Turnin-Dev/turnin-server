package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.presentation.dto.FindUserResultResponse
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest
import java.time.Instant

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = SocialLoginProviderForAuth.valueOf(provider.trim().uppercase()),
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    role = RoleForAuth.valueOf(role.trim().uppercase()),
    provider = SocialLoginProviderForAuth.valueOf(provider.trim().uppercase()),
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
    isActive = isActive,
    lastLoginAt = lastLoginAt?.let {
        Instant.ofEpochMilli(lastLoginAt)
    },
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)

fun FindUserResultDto.toResponse(): FindUserResultResponse = FindUserResultResponse(isExist)
