package com.peekr.domain.auth

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest

object AuthTestDoubles {
    val MockAuthUser = AuthUser(
        id = 0L,
        provider = SocialLoginProvider.Google,
        providerId = "providerIDDDDD",
        name = "honggd",
        nickname = "honggggg",
        profileImageUrl = "http://example.com/profile.jpg",
        introduce = "Hello!",
    )

    val MockValidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.Google.value,
        providerId = "providerIDDDDD",
    )

    val MockInvalidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.Google.value,
        providerId = "",
    )

    val MockValidRegisterRequest = RegisterRequest(
        provider = SocialLoginProvider.Google.value,
        providerId = "providerIDDDDD",
        name = "honggd",
        nickname = "honggggg",
        profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
        introduce = "Hello!",
    )

    val MockInvalidRegisterRequest = RegisterRequest(
        provider = SocialLoginProvider.Google.value,
        providerId = "providerIDDDDD",
        name = "",
        nickname = "",
        profileImageUrl = "aaaa",
        introduce = "Hello!",
    )

    val MockJWTTokenDto = JWTTokenDto(
        accessToken = "aaa.bbb.ccc",
        refreshToken = "aaa.bbb.ccc",
    )
}
