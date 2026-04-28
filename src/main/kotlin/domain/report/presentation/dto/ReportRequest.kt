package com.turnin.domain.report.presentation.dto

import com.turnin.domain.report.application.dto.ReportDetailDto
import kotlinx.serialization.Serializable

/**
 * 신고 요청 바디
 *
 * @param reporterId 신고자 ID
 * @param reportedId 신고 대상 사용자 ID
 * @param reportedUserKeywordId 신고 대상 사용자 키워드 ID
 * @param reasonId 신고 사유 ID
 * @param customReason 기타 신고 사유
 */
@Serializable
data class ReportRequest(
    val reporterId: Long,
    val reportedId: Long?,
    val reportedUserKeywordId: Long?,
    val reasonId: Long,
    val customReason: String? = null,
) {
    companion object {
        val sample = ReportRequest(
            reporterId = 1L,
            reportedId = 2L,
            reportedUserKeywordId = 1L,
            reasonId = 5L,
            customReason = "custom reason",
        )
    }
}

fun ReportRequest.toDto(): ReportDetailDto =
    ReportDetailDto(
        reporterId = reporterId,
        reportedId = reportedId,
        reportedUserKeywordId = reportedUserKeywordId,
        reasonId = reasonId,
        customReason = customReason,
    )
