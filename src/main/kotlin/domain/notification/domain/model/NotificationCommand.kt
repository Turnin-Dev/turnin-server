package com.peekr.domain.notification.domain.model

import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId

/**
 * 알림 저장 요청용 모델
 *
 * @property userId 사용자 ID
 * @property notiType 알림 유형
 * @property title 알림 제목
 * @property message 알림 본문
 * @property imageUrl 알림 첨부 이미지 URL
 * @property isBroadcast 브로드캐스트 여부
 * @param refId 참조 ID
 * @param refType 참조 타입
 */
data class NotificationCommand(
    val userId: UserId?,
    val notiType: NotificationType,
    val title: String?,
    val message: String,
    val imageUrl: String? = null,
    val isBroadcast: Boolean = false,
    val refId: Long? = null,
    val refType: String? = null,
)
