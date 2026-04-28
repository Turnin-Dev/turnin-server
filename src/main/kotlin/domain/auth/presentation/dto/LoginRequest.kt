package com.turnin.domain.auth.presentation.dto

import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.validator.PeekrValidator.validation
import com.turnin.domain.auth.application.dto.LoginDto
import kotlinx.serialization.Serializable

/**
 * 로그인 요청 바디
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 */
@Serializable
data class LoginRequest(
    val provider: SocialLoginProvider,
    val providerId: String,
) {
    companion object {
        val sample = LoginRequest(
            provider = SocialLoginProvider.GOOGLE,
            providerId = "1231312312312",
        )
    }
}

// ------------------------------ Mapper ------------------------------
fun LoginRequest.toDto(): LoginDto = LoginDto(
    provider = provider,
    providerId = providerId,
)

// ------------------------------ Validation ------------------------------
fun LoginRequest.validate() {
    validation(providerId.isNotBlank()) { "providerId가 존재하지 않습니다." }
}
