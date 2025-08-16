package com.peekr.domain.auth.application.mapper

import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.RoleForAuth
import java.time.Instant

object AuthMapper {
    fun RegisterDto.toDomain(): Register = Register(
        provider = provider,
        providerId = providerId,
        name = name,
        displayId = displayId,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
    )

    fun Register.toAuthUser(
        id: Long,
        role: RoleForAuth,
        isActive: Boolean,
        lastLoginAt: Instant?,
    ): AuthUser = AuthUser(
        id = id,
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

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(exists)
}
