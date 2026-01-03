package com.peekr.domain.auth.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import java.time.Instant

/**
 * 인증에 필요한 User 모델
 *
 * @param userId 사용자 ID
 * @param role 사용자 역할
 * @param provider 소셜로그인 플랫폼
 * @param providerId 소셜로그인 ID
 * @param displayId 사용자 표시 ID
 * @param userName 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 * @param isActive 사용자 활성 여부
 * @param lastLoginAt 마지막 로그인 일시
 */
data class AuthUser(
    val userId: UserId,
    val role: Role,
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: DisplayId,
    val userName: UserName,
    val profileImageUrl: String?,
    val introduce: Introduce?,
    val isActive: Boolean,
    val lastLoginAt: Instant?,
) {
    companion object {
        val sample: AuthUser = AuthUser(
            userId = UserId(1L),
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "123123123",
            displayId = DisplayId("hong_gd_123"),
            userName = UserName("honggd"),
            profileImageUrl = "https://example.com/image.jpg",
            introduce = Introduce("hello world!"),
            isActive = true,
            lastLoginAt = Instant.ofEpochMilli(1697875200000L),
        )
    }
}
