package com.peekr.domain.auth.application.mapper

import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.toSocialLoginProvider

object AuthMapper {
    fun RegisterDto.toDomain(): AuthUser = AuthUser(
        id = 0L,
        provider = provider.toSocialLoginProvider(),
        providerId = providerId,
        name = name,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
    )
}
