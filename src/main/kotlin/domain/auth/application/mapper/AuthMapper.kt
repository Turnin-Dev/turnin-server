package com.peekr.domain.auth.application.mapper

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.Register

object AuthMapper {
    fun RegisterDto.toDomain(): Register = Register(
        provider = provider,
        providerId = providerId,
        userName = UserName(name),
        displayId = DisplayId(displayId),
        profileImageUrl = profileImageUrl,
        introduce = introduce?.let { Introduce(it) },
    )

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(exists)
}
