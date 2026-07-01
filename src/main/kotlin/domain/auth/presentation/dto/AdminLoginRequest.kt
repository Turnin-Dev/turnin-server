package com.turnin.domain.auth.presentation.dto

import com.turnin.common.model.SocialLoginProvider
import kotlinx.serialization.Serializable

/**
 * 관리자용 로그인 요청 바디
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 * @property secretKey 관리자용 비밀키
 */
@Serializable
data class AdminLoginRequest(
    val provider: SocialLoginProvider,
    val providerId: String,
    val secretKey: String,
) {
    companion object {
        val sample = AdminLoginRequest(
            provider = SocialLoginProvider.GOOGLE,
            providerId = "1231312312312",
            secretKey = "hello-world!",
        )
    }
}

fun AdminLoginRequest.toLoginRequest() = LoginRequest(
    provider = provider,
    providerId = providerId,
)
