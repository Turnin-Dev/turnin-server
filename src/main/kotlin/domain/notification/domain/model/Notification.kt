package com.turnin.domain.notification.domain.model

import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.NotificationId
import com.turnin.common.model.id.UserId

/**
 * 알림 모델
 *
 * @property id 알림 ID
 * @property userId 사용자 ID
 * @property notiType 알림 유형
 * @property title 알림 제목
 * @property message 알림 본문
 * @property imageUrl 알림 첨부 이미지 URL
 * @property isRead 알림 읽음 여부
 * @property isBroadcast 브로드캐스트 여부
 * @param refId 참조 ID
 * @param refType 참조 타입
 * @param createdAt 생성 일자
 * @param updatedAt 수정 일자
 */
data class Notification(
    val id: NotificationId,
    val userId: UserId?,
    val notiType: NotificationType,
    val title: String?,
    val message: String,
    val imageUrl: String? = null,
    val isRead: Boolean,
    val isBroadcast: Boolean,
    val refId: Long? = null,
    val refType: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
