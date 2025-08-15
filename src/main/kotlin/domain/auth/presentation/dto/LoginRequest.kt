package com.peekr.domain.auth.presentation.dto

import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.validator.PeekrValidator.validation
import kotlinx.serialization.Serializable

/**
 * 로그인 요청 바디
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 */
@Serializable
data class LoginRequest(
    val provider: String,
    val providerId: String,
) {
    companion object {
        val sample = LoginRequest(
            provider = SocialLoginProvider.GOOGLE.name,
            providerId = "1231312312312",
        )
    }
}

fun LoginRequest.validate() {
    validation(provider.isNotBlank()) { "provider가 존재하지 않습니다." }
    validation(providerId.isNotBlank()) { "providerId가 존재하지 않습니다." }
}
