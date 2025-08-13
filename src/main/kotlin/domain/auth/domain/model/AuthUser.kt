package com.peekr.domain.auth.domain.model

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.db.scheme.UserRole

/**
 * 인증에 필요한 User 모델
 *
 * @param id 사용자 ID
 * @param role 사용자 역할
 * @param provider 소셜로그인 플랫폼 [SocialLoginProvider]
 * @param providerId 소셜로그인 ID
 * @param displayId 사용자 표시 ID
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class AuthUser(
    val id: Long,
    val role: UserRole,
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
) {
    companion object {
        val sample: AuthUser = AuthUser(
            id = 1L,
            role = UserRole.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "123123123",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "https://example.com/image.jpg",
            introduce = "hello world!",
        )
    }
}
