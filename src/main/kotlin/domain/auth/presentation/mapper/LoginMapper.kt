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
import com.peekr.domain.user.infrastructure.mapper.toRoleForAuth
import java.time.Instant

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    role = role.toRoleForAuth(),
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
    isActive = isActive,
    lastLoginAt = lastLoginAt?.let(Instant::ofEpochMilli),
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)

fun FindUserResultDto.toResponse(): FindUserResultResponse = FindUserResultResponse(isExist)
