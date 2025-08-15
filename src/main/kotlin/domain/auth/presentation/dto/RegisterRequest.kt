package com.peekr.domain.auth.presentation.dto

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.validator.PeekrValidator.validation
import kotlinx.serialization.Serializable

/** 회원가입 요청 바디 */
@Serializable
data class RegisterRequest(
    val role: Role,
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
    val isActive: Boolean = true,
    val lastLoginAt: Long? = null,
) {
    companion object {
        val sample = RegisterRequest(
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
            introduce = "Hello!",
            isActive = true,
            lastLoginAt = null,
        )
    }
}

fun RegisterRequest.validate() {
    validation(providerId.isNotBlank()) { "providerId는 존재하지 않습니다." }
    validation(name.isNotBlank() && name.length in 1..30) { "이름은 1~30자 이내여야 합니다." }
    validation(name.matches(Regex("^[a-zA-Z0-9가-힣]+$"))) {
        "이름은 영문/숫자/한글만 허용되며 1~30자여야 합니다."
    }
    validation(displayId.isNotBlank() && displayId.length in 1..30) { "ID는 1~30자 이내여야 합니다." }
    validation(displayId.matches(Regex("^[a-zA-Z0-9_]+$"))) {
        "ID는 영문/숫자/밑줄만 허용되며 1~30자여야 합니다."
    }
    validation(profileImageUrl == null || isValidUrl(profileImageUrl)) { "프로필 이미지 URL 형식이 올바르지 않습니다." }
    validation(introduce == null || introduce.length <= 200) { "자기소개는 200자 이내여야 합니다." }
}

private fun isValidUrl(url: String): Boolean {
    // http:// 또는 https://
    val urlRegex = Regex(
        pattern = "^https?://\\S+$",
        option = RegexOption.IGNORE_CASE,
    )
    return url.matches(urlRegex)
}
