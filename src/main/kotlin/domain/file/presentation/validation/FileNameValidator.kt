package com.peekr.domain.file.presentation.validation

import com.peekr.common.validator.PeekrValidator.validation

internal fun String?.validateFileNameAndReturn(): String {
    validation(!this.isNullOrBlank()) {
        "파일명이 비어있습니다."
    }
    validation(this!!.matches(Regex("^[A-Za-z0-9._-]{1,255}$"))) {
        "파일명은 최대 255자 이내의 영문/숫자/특수기호(., _, -)만 허용됩니다."
    }
    return this
}

internal fun String?.validateImageMimeAndReturn(): String {
    validation(!this.isNullOrBlank()) { "MIME 타입이 비어있습니다." }
    val normalized = this!!.trim().lowercase()
    validation(this.isImageType()) {
        "파일이 이미지 타입이 아닙니다."
    }
    return normalized
}

private fun String.isImageType(): Boolean =
    this.startsWith("image/") &&
        this.split("/").size == 2
