package com.peekr.domain.notification.presentation.dto

import com.peekr.domain.notification.application.dto.FcmTokenDto
import kotlinx.serialization.Serializable

/**
 * FCM 토큰 응답 바디
 *
 * @property id FCM 토큰 ID (사용자 FCM 토큰 ID)
 * @property userId 사용자 ID
 * @property token FCM 토큰
 * @property isActive 활성화 여부
 */
@Serializable
data class FcmTokenResponse(
    val id: Long,
    val userId: Long,
    val token: String,
    val isActive: Boolean,
) {
    companion object {
        val sample = FcmTokenResponse(
            id = 1L,
            userId = 1L,
            token = "fcm_token_example",
            isActive = true,
        )
    }
}

fun FcmTokenDto.toResponse() = FcmTokenResponse(
    id = this.id,
    userId = this.userId,
    token = this.token,
    isActive = this.isActive,
)
