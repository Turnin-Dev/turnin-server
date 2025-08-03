package com.peekr.domain.auth.application.mapper

import com.peekr.common.db.scheme.toSocialLoginProvider
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult

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

    fun FindUserResult.toDto(): FindUserResultDto = FindUserResultDto(isExist)
}
