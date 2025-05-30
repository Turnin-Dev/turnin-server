package com.peekr.domain.model.auth

/** 사용자 인증을 위한 AuthUser */
data class AuthUser(
    val id: Long,
    val provider: String,
    val providerId: String,
    val name: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val introduce: String?,
)
