package com.turnin.domain.report.application.dto

import com.turnin.domain.report.domain.model.ReportReason

/**
 * 신고 사유 DTO
 *
 * @param id 신고 사유 ID
 * @param code 신고 사유 코드
 * @param description 신고 사유 설명
 */
data class ReportReasonDto(
    val id: Long,
    val code: String,
    val description: String,
)

fun ReportReason.toDto(): ReportReasonDto =
    ReportReasonDto(
        id = this.id.value,
        code = this.code,
        description = this.description,
    )

fun List<ReportReason>.toDto(): List<ReportReasonDto> = this.map { it.toDto() }
