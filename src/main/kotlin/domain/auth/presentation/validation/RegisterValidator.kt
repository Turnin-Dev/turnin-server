package com.turnin.domain.auth.presentation.validation

import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.validator.TurninValidator.validation

internal fun String.validateDisplayId() {
    validation(
        this.isNotBlank() &&
            this.length in DisplayId.MIN_LENGTH..DisplayId.MAX_LENGTH,
    ) {
        "ID는 1~30자 이내여야 합니다."
    }
    validation(this.matches(DisplayId.RegexRule)) {
        "ID는 영문/숫자/밑줄(_)만 허용됩니다."
    }
}

internal fun String.validateName() {
    validation(
        this.isNotBlank() &&
            this.length in UserName.MIN_LENGTH..UserName.MAX_LENGTH,
    ) {
        "이름은 1~30자 이내여야 합니다."
    }
    validation(this.matches(UserName.RegexRule)) {
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
