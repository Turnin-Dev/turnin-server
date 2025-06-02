package com.peekr.application.dto.auth

import com.peekr.domain.model.value.auth.SocialLoginProvider

data class UserDto(
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val introduce: String?,
)
