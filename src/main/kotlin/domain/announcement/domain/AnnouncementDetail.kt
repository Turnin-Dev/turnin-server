package com.turnin.domain.announcement.domain

import com.turnin.common.model.AnnouncementAudience

/**
 * 공지 상세정보
 *
 * @property title 제목
 * @property content 내용
 * @property targetAudience 수신 대상
 * @property expiresAt 만료 일자
 */
data class AnnouncementDetail(
    val title: String,
    val content: String,
    val targetAudience: AnnouncementAudience,
    val expiresAt: Long?,
)
