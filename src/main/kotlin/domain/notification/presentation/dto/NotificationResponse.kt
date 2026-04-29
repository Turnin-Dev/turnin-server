package com.turnin.domain.notification.presentation.dto

import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.notification.application.dto.NotificationDto
import kotlinx.serialization.Serializable

/**
 * 알림 응답 바디
 *
 * @param id 알림 ID
 * @param notiType 알림 유형
 * @param title 알림 제목
 * @param message 알림 본문
 * @param imageUrl 알림 첨부 이미지 URL
 * @param isRead 읽음 여부
 * @param isBroadcast 브로드캐스트 여부
 * @param refId 참조 리소스 ID
 * @param refType 참조 리소스 타입
 * @param createdAt 생성 일자
 */
@Serializable
data class NotificationResponse(
    val id: Long,
    val userId: Long?,
    val notiType: String,
    val title: String?,
    val message: String,
    val imageUrl: String?,
    val isRead: Boolean,
    val isBroadcast: Boolean,
    val refId: Long?,
    val refType: String?,
    val createdAt: Long,
) {
    companion object {
        val sample = CursorPage(
            items = List(2) {
                NotificationResponse(
                    id = it + 1L,
                    userId = it + 1L,
                    notiType = "FRIEND_REQUEST",
                    title = "친구 요청",
                    message = "홍길동님이 친구 요청을 보냈어요.",
                    imageUrl = "https://example.com/profile.jpg",
                    isRead = false,
                    isBroadcast = false,
                    refId = it + 1L,
                    refType = "USER",
                    createdAt = 1716000000L,
                )
            },
            nextCursor = 2L,
        )
    }
}

fun NotificationDto.toResponse() = NotificationResponse(
    id = this.id,
    userId = this.userId,
    notiType = this.notiType.name,
    title = this.title,
    message = this.message,
    imageUrl = this.imageUrl,
    isRead = this.isRead,
    isBroadcast = this.isBroadcast,
    refId = this.refId,
    refType = this.refType,
    createdAt = this.createdAt,
)
