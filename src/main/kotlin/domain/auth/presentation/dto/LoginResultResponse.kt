package com.turnin.domain.auth.presentation.dto

import com.turnin.domain.auth.application.dto.LoginResultDto
import kotlinx.serialization.Serializable

/**
 * 로그인 결과 응답 바디
 *
 * @property userId 사용자 ID
 * @property accessToken JWT 액세스 토큰
 * @property refreshToken JWT 리프레쉬 토큰
 */
@Serializable
data class LoginResultResponse(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        val sample = LoginResultResponse(
            userId = 1,
            accessToken = "aaa.bbb.ccc",
            refreshToken = "aaa.bbb.ccc",
        )
    }
}

// ------------------------------ Mapper ------------------------------
fun LoginResultDto.toResponse(): LoginResultResponse = LoginResultResponse(
    userId = userId.value,
    accessToken = jwtTokenDto.accessToken,
    refreshToken = jwtTokenDto.refreshToken,
)
