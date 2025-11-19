package com.peekr.domain.report.application.dto

import com.peekr.domain.report.domain.model.ReportReason

data class ReportReasonDto(
    val code: String,
    val description: String,
)

fun ReportReason.toDto(): ReportReasonDto =
    ReportReasonDto(
        code = this.code,
        description = this.description,
    )

fun List<ReportReason>.toDto(): List<ReportReasonDto> = this.map { it.toDto() }
