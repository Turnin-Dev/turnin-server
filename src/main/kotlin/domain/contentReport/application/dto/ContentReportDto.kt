package com.turnin.domain.contentReport.application.dto

import com.turnin.common.model.ContentReportType

/**
 * 콘텐츠 신고 DTO
 *
 * @property contentType 신고 대상 콘텐츠 유형
 * @property contentId 신고 대상 콘텐츠 ID
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
data class ContentReportDto(
    val contentType: ContentReportType,
    val contentId: Long,
    val reasonId: Long,
    val customReason: String? = null,
)
