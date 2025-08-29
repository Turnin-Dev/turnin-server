package com.peekr.domain.file.exception

import com.peekr.common.exception.ApiErrorCode

sealed class FileErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object InvalidS3PresignerArgument :
        FileErrorCode(F001, "잘못된 요청 값으로 인해 업로드 URL을 생성할 수 없습니다.")
}

private const val F001 = "F001"
