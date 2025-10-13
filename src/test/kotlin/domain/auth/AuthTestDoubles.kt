package com.peekr.domain.auth

import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.auth.application.dto.LoginResultDto
import com.peekr.domain.auth.application.dto.RegisterResultDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

internal object AuthTestDoubles {
    private val MockUserId = UserId(1L)
    val MockAuthUser = AuthUser(
        userId = MockUserId,
        role = RoleForAuth.USER,
        provider = SocialLoginProviderForAuth.GOOGLE,
        providerId = "providerIDDDDD",
        displayId = DisplayId("hong_gd_123"),
        name = Name("honggd"),
        profileImageUrl = "http://example.com/profile.jpg",
        introduce = "Hello!",
        isActive = true,
        lastLoginAt = null,
    )

    val MockRegister = Register(
        provider = SocialLoginProviderForAuth.GOOGLE,
        providerId = "providerIDDDDD",
        displayId = DisplayId("hong_gd_123"),
        name = Name("honggd"),
        profileImageUrl = "http://example.com/profile.jpg",
        introduce = "Hello!",
    )

    val MockValidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.GOOGLE,
        providerId = "providerIDDDDD",
    )

    val MockInvalidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.GOOGLE,
        providerId = "",
    )

    val MockValidRegisterRequest = RegisterRequest(
        provider = SocialLoginProvider.GOOGLE,
        providerId = "providerIDDDDD",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
        introduce = "Hello!",
    )

    val MockInvalidRegisterRequest = RegisterRequest(
        provider = SocialLoginProvider.GOOGLE,
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

    val MockLoginResultDto = LoginResultDto(
        userId = MockUserId,
        jwtTokenDto = MockJWTTokenDto,
    )

    val MockRegisterResultDto = RegisterResultDto(
        userId = MockUserId,
        jwtTokenDto = MockJWTTokenDto,
    )
}
