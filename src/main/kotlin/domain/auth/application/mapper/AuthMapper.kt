package com.peekr.domain.auth.application.mapper

import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.Register

object AuthMapper {
    fun RegisterDto.toDomain(): Register = Register(
        provider = provider,
        providerId = providerId,
        name = name,
        displayId = displayId,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
    )

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(exists)
}
