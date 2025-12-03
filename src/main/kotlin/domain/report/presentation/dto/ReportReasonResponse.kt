package com.peekr.domain.report.presentation.dto

import com.peekr.domain.report.application.dto.ReportReasonDto
import kotlinx.serialization.Serializable

/**
 * 신고 사유 응답 바디
 *
 * @param id 신고 사유 ID
 * @param code 신고 사유 코드
 * @param description 신고 사유 설명
 */
@Serializable
data class ReportReasonResponse(
    val id: Long,
    val code: String,
    val description: String,
) {
    companion object {
        val sample = ReportReasonResponse(1L, "code", "description")
    }
}

fun ReportReasonDto.toResponse() = ReportReasonResponse(id, code, description)
