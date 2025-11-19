package com.peekr.domain.report.presentation.dto

import com.peekr.domain.report.application.dto.ReportReasonDto
import kotlinx.serialization.Serializable

/**
 * 신고 사유 응답 바디
 */
@Serializable
data class ReportReasonResponse(
    val code: String,
    val description: String,
) {
    companion object {
        val sample = ReportReasonResponse("code", "description")
    }
}

fun ReportReasonDto.toResponse() = ReportReasonResponse(code, description)
