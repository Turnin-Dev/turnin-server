package com.peekr.common.exception.common

import com.peekr.common.exception.ApiErrorCode

sealed class CommonErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object MalformedRequest :
        CommonErrorCode(
            ErrorCodes.Malformed.Request.code,
            ErrorCodes.Malformed.Request.description,
        )

    data object ValidationDefault :
        CommonErrorCode(
            ErrorCodes.Validation.Default.code,
            ErrorCodes.Validation.Default.description,
        )

    data object EmptyRequestHeader :
        CommonErrorCode(
            ErrorCodes.EmptyRequest.Header.code,
            ErrorCodes.EmptyRequest.Header.description,
        )

    data object EmptyRequestParam :
        CommonErrorCode(
            ErrorCodes.EmptyRequest.Parameter.code,
            ErrorCodes.EmptyRequest.Parameter.description,
        )

    data object DomainError :
        CommonErrorCode(
            ErrorCodes.Domain.Invalid.code,
            ErrorCodes.Domain.Invalid.description,
        )

    data object AccessDenied :
        CommonErrorCode(
            ErrorCodes.Auth.AccessDenied.code,
            ErrorCodes.Auth.AccessDenied.description,
        )

    object Unexpected : CommonErrorCode("UN001", "알 수 없는 오류입니다.")
}

private object ErrorCodes {
    enum class Malformed(
        val code: String,
        val description: String,
    ) {
        Request("MAL001", "잘못된 형식의 요청입니다."),
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
        Parameter("EMP002", "요청 파라미터 값이 비어있습니다."),
    }

    enum class Domain(
        val code: String,
        val description: String,
    ) {
        Invalid("D001", "도메인 규칙 에러"),
    }

    enum class Auth(
        val code: String,
        val description: String,
    ) {
        AccessDenied("AD001", "액세스 접근 불가"),
    }
}
