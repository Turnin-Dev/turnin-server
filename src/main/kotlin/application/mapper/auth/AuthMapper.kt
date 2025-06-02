package com.peekr.application.mapper.auth

import com.peekr.application.dto.auth.UserDto
import com.peekr.domain.model.entity.auth.AuthUser

object AuthMapper {
    fun UserDto.toDomain(): AuthUser =
        AuthUser(
            id = 0L,
            provider = provider,
            providerId = providerId,
            name = name.orEmpty(),
            nickname = nickname.orEmpty(),
            profileImageUrl = profileImageUrl,
            introduce = introduce,
        )
}
