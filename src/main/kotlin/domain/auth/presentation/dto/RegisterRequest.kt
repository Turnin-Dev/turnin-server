package com.peekr.domain.auth.presentation.dto

import com.peekr.common.validator.PeekrValidator.validation
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val provider: String,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
) {
    fun validate() {
        validation(providerId.isNotBlank()) { "providerId는 존재하지 않습니다." }
        validation(name.isNotBlank() && name.length in 1..30) { "이름은 1~30자 이내여야 합니다." }
        validation(nickname.isNotBlank() && nickname.length in 1..30) { "닉네임은 1~30자 이내여야 합니다." }
        validation(nickname.matches(Regex("^[a-zA-Z0-9가-힣_]{1,30}$"))) {
            "닉네임은 영문/숫자/한글/밑줄만 허용되며 1~30자여야 합니다."
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
}
