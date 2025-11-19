package com.peekr.domain.report.presentation.dto

import com.peekr.domain.report.application.dto.ReportDto
import kotlinx.serialization.Serializable

/**
 * 신고 요청 바디
 */
@Serializable
data class ReportRequest(
    val reporterId: Long,
    val reportedId: Long,
    val reasonId: Long,
    val customReason: String? = null,
) {
    companion object {
        val sample = ReportRequest(
            reporterId = 1L,
            reportedId = 2L,
            reasonId = 5L,
            customReason = "custom reason",
        )
    }
}

fun ReportRequest.toDto(): ReportDto =
    ReportDto(
        reporterId = reporterId,
        reportedId = reportedId,
        reasonId = reasonId,
        customReason = customReason,
    )
