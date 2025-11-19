package com.peekr.domain.report.application.usecase

import com.peekr.domain.report.application.dto.ReportReasonDto
import com.peekr.domain.report.application.dto.toDto
import com.peekr.domain.report.domain.repository.ReportRepository

class GetReportReasonsUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(): List<ReportReasonDto> =
        reportRepository.getReportReasons().toDto()
}
