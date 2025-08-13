package com.peekr.domain.auth

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.db.scheme.UserRole
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

object AuthTestDoubles {
    val MockAuthUser = AuthUser(
        id = 0L,
        role = UserRole.USER,
        provider = SocialLoginProvider.GOOGLE,
        providerId = "providerIDDDDD",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "http://example.com/profile.jpg",
        introduce = "Hello!",
    )

    val MockValidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.GOOGLE.name,
        providerId = "providerIDDDDD",
    )

    val MockInvalidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.GOOGLE.name,
        providerId = "",
    )

    val MockValidRegisterRequest = RegisterRequest(
        role = UserRole.USER.name,
        provider = SocialLoginProvider.GOOGLE.name,
        providerId = "providerIDDDDD",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
        introduce = "Hello!",
    )

    val MockInvalidRegisterRequest = RegisterRequest(
        role = UserRole.USER.name,
        provider = SocialLoginProvider.GOOGLE.name,
        providerId = "providerIDDDDD",
        displayId = "",
        name = "",
        profileImageUrl = "aaaa",
        introduce = "Hello!",
    )

    val MockJWTTokenDto = JWTTokenDto(
        accessToken = "aaa.bbb.ccc",
        refreshToken = "aaa.bbb.ccc",
    )
}
