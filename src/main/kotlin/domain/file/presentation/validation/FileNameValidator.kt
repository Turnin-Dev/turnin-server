package com.turnin.domain.file.presentation.validation

import com.turnin.common.validator.TurninValidator.validation
import com.turnin.domain.file.domain.model.FileCategory
import com.turnin.domain.file.domain.model.FileCategory.Companion.toFileCategory

internal fun String?.validateFileNameAndReturn(): String {
    validation(!this.isNullOrBlank()) {
        "파일명이 비어있습니다."
    }
    validation(this!!.matches(FILE_NAME_REGEX)) {
        "파일명은 최대 255자 이내의 한글/영문/숫자/특수기호(., _, -)만 허용됩니다."
    }
    return this
}

internal fun String?.validateImageMimeAndReturn(): String {
    validation(!this.isNullOrBlank()) { "MIME 타입이 비어있습니다." }
    val normalized = this!!.trim().lowercase()
    validation(normalized.isImageType()) {
        "파일이 이미지 타입이 아닙니다."
    }
    return normalized
}

internal fun String?.validateFileCategory(): FileCategory {
    validation(!this.isNullOrBlank()) { "파일 카테고리가 비어있습니다." }
    val category = this?.toFileCategory()
    validation(category != null) { "지원하지 않는 파일 카테고리입니다." }
    return category!!
}

private fun String.isImageType(): Boolean = IMAGE_MIME_REGEX.matches(this)

private val IMAGE_MIME_REGEX = Regex("^image/[a-z0-9][a-z0-9.+-]{0,127}$")
private val FILE_NAME_REGEX = Regex("^[A-Za-z0-9가-힣._-]{1,255}$")
