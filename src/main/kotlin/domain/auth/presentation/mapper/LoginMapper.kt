package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProviderForAuth
import com.peekr.domain.auth.presentation.dto.ExistsResultResponse
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest
import com.peekr.domain.core.model.DisplayId
import com.peekr.domain.core.model.Name

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    provider = provider.toSocialLoginProviderForAuth(),
    providerId = providerId,
    displayId = DisplayId(displayId),
    name = Name(name),
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)

fun FindUserResultDto.toResponse(): ExistsResultResponse = ExistsResultResponse(exists)
