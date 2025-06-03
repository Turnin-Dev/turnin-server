package com.peekr.domain.auth.domain.model.entity

import com.peekr.domain.auth.domain.model.value.SocialLoginProvider

data class AuthUser(
    val id: Long,
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
)
