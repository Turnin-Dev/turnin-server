package com.peekr.domain.auth.presentation.validation

import com.peekr.common.validator.PeekrValidator.validation

internal fun String.validateDisplayId() {
    validation(this.isNotBlank() && this.length in 1..30) { "ID는 1~30자 이내여야 합니다." }
    validation(this.matches(Regex("^[a-zA-Z0-9_]+$"))) {
        "ID는 영문/숫자/밑줄(_)만 허용됩니다."
    }
}

internal fun String.validateName() {
    validation(this.isNotBlank() && this.length in 1..30) { "이름은 1~30자 이내여야 합니다." }
    validation(this.matches(Regex("^[a-zA-Z0-9가-힣]+$"))) {
        "이름은 영문/숫자/한글만 허용됩니다."
    }
}

internal fun String.validateProviderId() {
    validation(this.isNotBlank()) { "providerId는 존재하지 않습니다." }
}

internal fun String.validateProfileImageUrl() {
    validation(isValidUrl(this)) { "프로필 이미지 URL 형식이 올바르지 않습니다." }
}

internal fun String.validateIntroduce() {
    validation(this.length <= 200) { "자기소개는 200자 이내여야 합니다." }
}

private fun isValidUrl(url: String): Boolean {
    // http:// 또는 https://
    val urlRegex = Regex(
        pattern = "^https?://\\S+$",
        option = RegexOption.IGNORE_CASE,
    )
    return url.matches(urlRegex)
}
