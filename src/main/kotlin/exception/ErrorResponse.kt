package com.peekr.exception

import kotlinx.serialization.Serializable

/**
 * 공통적으로 사용하는 에러 응답 바디
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val status: Int,
)
