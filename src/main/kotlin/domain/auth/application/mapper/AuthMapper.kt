package com.peekr.domain.auth.application.mapper

import com.peekr.common.jwt.application.dto.JwtTokenDto
import com.peekr.common.jwt.domain.model.entity.JwtToken
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

    fun JwtToken?.toDto(): JwtTokenDto? = this?.let {
        JwtTokenDto(accessToken, refreshToken)
    }
}
