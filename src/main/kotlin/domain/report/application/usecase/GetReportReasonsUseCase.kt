package com.turnin.domain.report.application.usecase

import com.turnin.domain.report.application.dto.ReportReasonDto
import com.turnin.domain.report.application.dto.toDto
import com.turnin.domain.report.domain.repository.ReportRepository

class GetReportReasonsUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(): List<ReportReasonDto> =
        reportRepository.getReportReasons().toDto()
}
