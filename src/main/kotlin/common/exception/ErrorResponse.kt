package com.peekr.common.exception

import kotlinx.serialization.Serializable

/**
 * 공통적으로 사용하는 에러 응답 바디
 *
 * @param code 에러 코드
 * @param message 에러 메시지
 * @param status 상태 코드 (Http 상태 코드)
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val status: Int,
)
