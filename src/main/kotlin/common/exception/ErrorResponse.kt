package com.peekr.common.exception

import kotlinx.serialization.Serializable

/**
 * 공통적으로 사용하는 에러 응답 바디
 *
 * 클라이언트에게 노출될 수 있으므로 민감한 정보는 포함하지 않는다.
 *
 * @param code 에러 코드
 * @param status 상태 코드 (Http 상태 코드)
 * @param message 에러 메시지
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val status: Int,
    val message: String,
)
