package com.peekr.domain.auth.application.dto

data class RegisterDto(
    val provider: String,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
)
