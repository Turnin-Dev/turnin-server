package com.peekr.domain.auth.application.mapper

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.domain.model.entity.AuthUser

object AuthMapper {
    fun LoginDto.toDomain(): AuthUser =
        AuthUser(
            id = 0L,
            provider = provider,
            providerId = providerId,
            name = name.orEmpty(),
            nickname = nickname.orEmpty(),
            profileImageUrl = profileImageUrl,
            introduce = introduce,
        )

    fun JWTToken?.toDto(): JWTTokenDto? = this?.let {
        JWTTokenDto(accessToken, refreshToken)
    }
}
