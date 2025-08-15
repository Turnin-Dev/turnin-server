package com.peekr.domain.auth.application.mapper

import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult

object AuthMapper {
    fun RegisterDto.toDomain(): AuthUser = AuthUser(
        id = 0L,
        role = role,
        provider = provider,
        providerId = providerId,
        name = name,
        displayId = displayId,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
        isActive = isActive,
        lastLoginAt = lastLoginAt,
    )

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(isExist)
}
