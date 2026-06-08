package com.turnin.domain.announcement.presentation.dto

import com.turnin.common.model.AnnouncementAudience
import com.turnin.domain.announcement.application.dto.AnnouncementDto
import kotlinx.serialization.Serializable

/**
 * 공지 응답 바디
 *
 * @property id 공지 ID
 * @property title 제목
 * @property content 내용
 * @property targetAudience 수신 대상
 * @property expiresAt 만료 일자
 * @property createdAt 생성 일자
 * @property isRead 읽음 여부
 */
@Serializable
data class AnnouncementResponse(
    val id: Long,
    val title: String,
    val content: String,
    val targetAudience: AnnouncementAudience,
    val expiresAt: Long?,
    val createdAt: Long,
    val isRead: Boolean,
) {
    companion object {
        val sample = listOf(
            AnnouncementResponse(
                id = 1L,
                title = "공지 제목",
                content = "공지 내용",
                targetAudience = AnnouncementAudience.ALL,
                expiresAt = null,
                createdAt = 1714000000L,
                isRead = false,
            ),
        )
    }
}

fun AnnouncementDto.toResponse() = AnnouncementResponse(
    id = id,
    title = title,
    content = content,
    targetAudience = targetAudience,
    expiresAt = expiresAt,
    createdAt = createdAt,
    isRead = isRead,
)
