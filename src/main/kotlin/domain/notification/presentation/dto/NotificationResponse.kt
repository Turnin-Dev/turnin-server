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
 * @param refData 딥링크용 부가 데이터 (FCM data와 동일한 key-value 맵, 예: 키워드 게시물 작성자 ID `ref_owner_id`)
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
    val refData: Map<String, String>?,
    val createdAt: Long,
) {
    companion object {
        val sample = CursorPage(
            items = listOf(
                NotificationResponse(
                    id = 2L,
                    userId = 1L,
                    notiType = "PING_PONG_ANSWER",
                    title = "새 답변",
                    message = "홍길동 님이 질문에 답변했어요.",
                    imageUrl = null,
                    isRead = false,
                    isBroadcast = false,
                    refId = 3L,
                    refType = "KEYWORD",
                    refData = mapOf("ref_owner_id" to "34"),
                    createdAt = 1716000100L,
                ),
                NotificationResponse(
                    id = 1L,
                    userId = 1L,
                    notiType = "FRIEND_REQUEST",
                    title = "친구 요청",
                    message = "홍길동님이 친구 요청을 보냈어요.",
                    imageUrl = "https://example.com/profile.jpg",
                    isRead = false,
                    isBroadcast = false,
                    refId = 34L,
                    refType = "USER",
                    refData = null,
                    createdAt = 1716000000L,
                ),
            ),
            nextCursor = 1L,
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
    refData = this.refData,
    createdAt = this.createdAt,
)
