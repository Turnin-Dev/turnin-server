package com.peekr.domain.notification.presentation.dto

import kotlinx.serialization.Serializable

/**
 * FCM 토큰 등록 요청 바디
 *
 * @property token FCM 토큰
 */
@Serializable
data class RegisterFcmTokenRequest(val token: String) {
    companion object {
        val sample = RegisterFcmTokenRequest(
            token = "fcm_token_example",
        )
    }
}
