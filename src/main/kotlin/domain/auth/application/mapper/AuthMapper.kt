package com.turnin.domain.auth.application.mapper

import com.turnin.common.model.Introduce
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.domain.auth.application.dto.FindUserResultDto
import com.turnin.domain.auth.application.dto.RegisterDto
import com.turnin.domain.auth.domain.model.FindUserResult
import com.turnin.domain.auth.domain.model.Register

object AuthMapper {
    fun RegisterDto.toDomain(): Register = Register(
        provider = provider,
        providerId = providerId,
        userName = UserName(name),
        displayId = DisplayId(displayId),
        profileImageUrl = profileImageUrl,
        introduce = Introduce(introduce),
    )

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(exists)
}
