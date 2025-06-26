package com.peekr.common.exception

sealed class CommonErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object MalformedRequest :
        CommonErrorCode(
            ErrorCodes.Malformed.Request.code,
            ErrorCodes.Malformed.Request.description,
        )

    data object Validation :
        CommonErrorCode(
            ErrorCodes.Validation.Default.code,
            ErrorCodes.Validation.Default.description,
        )

    data object EmptyRequestHeader :
        CommonErrorCode(
            ErrorCodes.EmptyRequest.Header.code,
            ErrorCodes.EmptyRequest.Header.description,
        )

    object Unexpected : CommonErrorCode("UN001", "예상하지 못한 오류입니다.")
}

private object ErrorCodes {
    enum class Malformed(
        val code: String,
        val description: String,
    ) {
        Request("MAL001", "잘못된 형식의 요청 바디입니다."),
    }

    enum class Validation(
        val code: String,
        val description: String,
    ) {
        Default("VD001", "기본 유효성 검사 실패"),
    }

    enum class EmptyRequest(
        val code: String,
        val description: String,
    ) {
        Header("EMP001", "요청 헤더 값이 비어있습니다."),
    }
}
