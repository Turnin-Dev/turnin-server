package com.peekr.domain.auth

import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider

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
}
