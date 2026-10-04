package com.turnin.domain.contentReport.presentation.dto

import com.turnin.common.model.ContentReportType
import com.turnin.domain.contentReport.application.dto.ContentReportDto
import kotlinx.serialization.Serializable

/**
 * 콘텐츠 신고 요청 바디
 *
 * @property contentType 신고 대상 콘텐츠 유형
 * @property contentId 신고 대상 콘텐츠 ID (Ex. 질문: 핑퐁 ID, 답변: 핑퐁 답변 ID)
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
@Serializable
data class CreateContentReportRequest(
    val contentType: ContentReportType,
    val contentId: Long,
    val reasonId: Long,
    val customReason: String? = null,
) {
    companion object {
        val sample = CreateContentReportRequest(
            contentType = ContentReportType.PING_PONG_QUESTION,
            contentId = 1L,
            reasonId = 5L,
            customReason = "custom reason",
        )
    }
}

fun CreateContentReportRequest.toDto(): ContentReportDto =
    ContentReportDto(
        contentType = contentType,
        contentId = contentId,
        reasonId = reasonId,
        customReason = customReason,
    )
