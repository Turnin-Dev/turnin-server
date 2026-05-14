package com.turnin.domain.notification.application.dto

import com.turnin.domain.notification.domain.model.FcmToken

/**
 * FCM 토큰 DTO
 *
 * @param id FCM 토큰 ID
 * @param userId 사용자 ID
 * @param token FCM 토큰
 * @param isActive 활성화 여부
 * @param createdAt 생성 일자
 * @param updatedAt 수정 일자
 */
data class FcmTokenDto(
    val id: Long,
    val userId: Long,
    val token: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

fun FcmToken.toDto() = FcmTokenDto(
    id = this.id.value,
    userId = this.userId.value,
    token = this.token,
    isActive = this.isActive,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
