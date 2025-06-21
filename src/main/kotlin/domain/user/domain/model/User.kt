package com.peekr.domain.user.domain.model

import com.peekr.domain.auth.domain.model.SocialLoginProvider

/**
 * User
 *
 * @param id 사용자 ID
 * @param provider 소셜로그인 플랫폼 [SocialLoginProvider]
 * @param providerId 소셜로그인 ID
 * @param name 사용자 이름
 * @param nickname 사용자 닉네임
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class User(
    val id: Long,
    val provider: String,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
)
