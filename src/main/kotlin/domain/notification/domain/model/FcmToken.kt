package com.peekr.domain.notification.domain.model

import com.peekr.common.model.id.FcmTokenId
import com.peekr.common.model.id.UserId

/**
 * 사용자 FCM 토큰 모델
 *
 * @property id 사용자 FCM 토큰 ID
 * @property userId 사용자 ID
 * @property token 토큰
 * @property isActive 활성화 여부
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class FcmToken(
    val id: FcmTokenId,
    val userId: UserId,
    val token: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
