package com.peekr.domain.auth.presentation.dto

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.validator.PeekrValidator.validation
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val provider: String,
    val providerId: String,
) {
    fun validate() {
        validation(provider.isNotBlank()) { "provider가 존재하지 않습니다." }
        validation(providerId.isNotBlank()) { "providerId가 존재하지 않습니다." }
    }

    companion object {
        val sample = LoginRequest(
            provider = SocialLoginProvider.GOOGLE.name,
            providerId = "1231312312312",
        )
    }
}
