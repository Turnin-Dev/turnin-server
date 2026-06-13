package com.turnin.domain.auth.presentation.dto

import com.turnin.common.model.SocialLoginProvider
import kotlinx.serialization.Serializable

/**
 * 관리자용 회원가입 요청 바디
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 * @property introduce 사용자 소개 글
 * @property secretKey 관리자용 비밀키
 */
@Serializable
data class AdminRegisterRequest(
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String? = null,
    val introduce: String,
    val secretKey: String,
) {
    companion object {
        val sample = AdminRegisterRequest(
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "http://example.com/profile.jpg",
            introduce = "Hello!",
            secretKey = "hello-world!",
        )
    }
}

fun AdminRegisterRequest.toRegisterRequest(): RegisterRequest =
    RegisterRequest(
        provider = provider,
        providerId = providerId,
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
    )
