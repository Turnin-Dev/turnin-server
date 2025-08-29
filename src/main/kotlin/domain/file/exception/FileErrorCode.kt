package com.peekr.domain.file.exception

import com.peekr.common.exception.ApiErrorCode

sealed class FileErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object InvalidS3PresignerArgument :
        FileErrorCode(F001, "서버 내부에서 파일을 저장하는 과정에서 에러가 발생했습니다.")
}

private const val F001 = "F001"
