package com.peekr.application.mapper.auth

import com.peekr.application.dto.auth.JwtTokenDto
import com.peekr.application.dto.auth.LoginDto
import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.entity.auth.JwtToken

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
