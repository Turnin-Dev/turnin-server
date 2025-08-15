package com.peekr.domain.auth

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

object AuthTestDoubles {
    val MockAuthUser = AuthUser(
        id = 0L,
        role = RoleForAuth.USER,
        provider = SocialLoginProviderForAuth.GOOGLE,
        providerId = "providerIDDDDD",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "http://example.com/profile.jpg",
        introduce = "Hello!",
        isActive = true,
        lastLoginAt = null,
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
        role = Role.USER.name,
        provider = SocialLoginProvider.GOOGLE.name,
        providerId = "providerIDDDDD",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
        introduce = "Hello!",
    )

    val MockInvalidRegisterRequest = RegisterRequest(
        role = Role.USER.name,
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
