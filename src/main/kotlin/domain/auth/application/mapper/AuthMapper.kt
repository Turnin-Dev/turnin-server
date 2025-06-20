package com.peekr.domain.auth.application.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.toSocialLoginProvider

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

    fun JWTToken?.toDto(): JWTTokenDto? = this?.let {
        JWTTokenDto(accessToken, refreshToken)
    }
}
