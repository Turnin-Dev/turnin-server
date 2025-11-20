package com.peekr.domain.report.presentation.dto

import com.peekr.domain.report.application.dto.ReportReasonDto
import kotlinx.serialization.Serializable

/**
 * 신고 사유 목록 응답 바디
 */
@Serializable
data class ReportReasonsResponse(val reasons: List<ReportReasonResponse>) {
    companion object {
        val sample = ReportReasonsResponse(
            reasons = listOf(ReportReasonResponse.sample),
        )
    }
}

fun List<ReportReasonDto>.toResponse() =
    ReportReasonsResponse(
        reasons = map { it.toResponse() },
    )
