package com.turnin.domain.announcement.application.dto

import com.turnin.common.model.AnnouncementAudience
import com.turnin.domain.announcement.domain.model.Announcement

/**
 * 공지 DTO
 *
 * @property id 공지 ID
 * @property title 제목
 * @property content 내용
 * @property targetAudience 수신 대상
 * @property expiresAt 만료 일자
 * @property createdAt 생성 일자
 * @property isRead 읽음 여부
 */
data class AnnouncementDto(
    val id: Long,
    val title: String,
    val content: String,
    val targetAudience: AnnouncementAudience,
    val expiresAt: Long?,
    val createdAt: Long,
    val isRead: Boolean,
)

fun Announcement.toDto(): AnnouncementDto =
    AnnouncementDto(
        id = id.value,
        title = title,
        content = content,
        targetAudience = targetAudience,
        expiresAt = expiresAt,
        createdAt = createdAt,
        isRead = isRead,
    )
