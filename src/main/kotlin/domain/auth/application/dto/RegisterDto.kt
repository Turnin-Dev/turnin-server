package com.peekr.domain.auth.application.dto

data class RegisterDto(
    val role: String,
    val provider: String,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
)
