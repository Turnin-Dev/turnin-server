package com.peekr.domain.auth.presentation.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.LoginResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.dto.RegisterResultDto
import com.peekr.domain.auth.presentation.dto.ExistsResultResponse
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.LoginResultResponse
import com.peekr.domain.auth.presentation.dto.RegisterRequest
import com.peekr.domain.auth.presentation.dto.RegisterResultResponse

fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider,
    providerId = providerId,
)

fun RegisterRequest.toDto(): RegisterDto = RegisterDto(
    provider = provider,
    providerId = providerId,
    displayId = DisplayId(displayId),
    name = Name(name),
    profileImageUrl = profileImageUrl,
    introduce = introduce?.let { Introduce(it) },
)

fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)

fun FindUserResultDto.toResponse(): ExistsResultResponse = ExistsResultResponse(exists)

fun LoginResultDto.toResponse(): LoginResultResponse = LoginResultResponse(
    userId = userId.value,
    accessToken = jwtTokenDto.accessToken,
    refreshToken = jwtTokenDto.refreshToken,
)

fun RegisterResultDto.toResponse(): RegisterResultResponse = RegisterResultResponse(
    userId = userId.value,
    accessToken = jwtTokenDto.accessToken,
    refreshToken = jwtTokenDto.refreshToken,
)
