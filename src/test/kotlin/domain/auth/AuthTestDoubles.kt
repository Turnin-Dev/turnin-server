package com.peekr.domain.auth

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.presentation.dto.LoginRequest

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
        provider = SocialLoginProvider.Google.name,
        providerId = "providerIDDDDD",
    )

    val MockInvalidLoginRequest = LoginRequest(
        provider = SocialLoginProvider.Google.name,
        providerId = "",
    )

//    val MockValidLoginRequest = LoginRequest(
//        provider = SocialLoginProvider.Google.name,
//        providerId = "providerIDDDDD",
//        name = "honggd",
//        nickname = "honggggg",
//        profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
//        introduce = "Hello!",
//    )

    val MockJWTTokenDto = JWTTokenDto(
        accessToken = "aaa.bbb.ccc",
        refreshToken = "aaa.bbb.ccc",
    )
}
