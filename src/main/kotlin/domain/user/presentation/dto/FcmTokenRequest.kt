package com.peekr.domain.user.presentation.dto

import kotlinx.serialization.Serializable

/**
 * FCM 토큰 요청 바디
 *
 * @property token FCM 토큰
 */
@Serializable
data class FcmTokenRequest(val token: String) {
    companion object {
        val sample = FcmTokenRequest("fcm-token")
    }
}
