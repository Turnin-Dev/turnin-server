package com.peekr.domain.auth.presentation.dto

import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.validator.PeekrValidator.validation
import kotlinx.serialization.Serializable

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
    validation(provider.trim().isNotBlank()) { "provider가 존재하지 않습니다." }
    validation(providerId.trim().isNotBlank()) { "providerId가 존재하지 않습니다." }
}
