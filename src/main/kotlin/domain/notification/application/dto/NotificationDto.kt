package com.peekr.domain.notification.application.dto

import com.peekr.common.model.NotificationType
import com.peekr.domain.notification.domain.model.Notification

/**
 * 알림 DTO
 *
 * @param id 알림 ID
 * @param userId 사용자 ID
 * @param notiType 알림 유형
 * @param title 알림 제목
 * @param message 알림 본문
 * @param imageUrl 알림 첨부 이미지 URL
 * @param isRead 읽음 여부
 * @param isBroadcast 브로드캐스트 여부
 * @param refId 참조 리소스 ID
 * @param refType 참조 리소스 타입
 * @param createdAt 생성 일자
 * @param updatedAt 수정 일자
 */
data class NotificationDto(
    val id: Long,
    val userId: Long?,
    val notiType: NotificationType,
    val title: String?,
    val message: String,
    val imageUrl: String?,
    val isRead: Boolean,
    val isBroadcast: Boolean,
    val refId: Long?,
    val refType: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Notification.toDto() = NotificationDto(
    id = this.id.value,
    userId = this.userId?.value,
    notiType = this.notiType,
    title = this.title,
    message = this.message,
    imageUrl = this.imageUrl,
    isRead = this.isRead,
    isBroadcast = this.isBroadcast,
    refId = this.refId,
    refType = this.refType,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
