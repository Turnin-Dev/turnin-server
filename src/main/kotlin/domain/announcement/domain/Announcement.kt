package com.turnin.domain.announcement.domain

import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.id.AnnouncementId

/**
 * 공지 모델
 *
 * @property id 공지 ID
 * @property title 제목
 * @property content 내용
 * @property targetAudience 수신 대상
 * @property expiresAt 만료 일자
 * @property createdAt 생성 일자
 * @property isRead 읽음 여부
 */
data class Announcement(
    val id: AnnouncementId,
    val title: String,
    val content: String,
    val targetAudience: AnnouncementAudience,
    val expiresAt: Long?,
    val createdAt: Long,
    val isRead: Boolean,
)
