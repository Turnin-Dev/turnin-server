package com.peekr.common.exception

sealed class CommonErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object MalformedRequest :
        CommonErrorCode(
            CommonErrorCodes.Malformed.Request.code,
            CommonErrorCodes.Malformed.Request.description,
        )
}

private object CommonErrorCodes {
    enum class Malformed(
        val code: String,
        val description: String,
    ) {
        Request("MAL001", "잘못된 형식의 요청 바디입니다."),
    }
}
