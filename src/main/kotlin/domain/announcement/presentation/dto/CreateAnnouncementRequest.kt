package com.turnin.domain.announcement.presentation.dto

import com.turnin.common.model.AnnouncementAudience
import com.turnin.domain.announcement.domain.model.AnnouncementDetail
import kotlinx.serialization.Serializable

/**
 * 공지 생성 요청 바디
 *
 * @property title 제목
 * @property content 내용
 * @property targetAudience 수신 대상
 * @property expiresAt 만료 일자
 */
@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val content: String,
    val targetAudience: AnnouncementAudience,
    val expiresAt: Long? = null,
) {
    fun toDto() = AnnouncementDetail(
        title = title,
        content = content,
        targetAudience = targetAudience,
        expiresAt = expiresAt,
    )

    companion object {
        val sample = CreateAnnouncementRequest(
            title = "공지 제목",
            content = "공지 내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = 1000,
        )
    }
}
