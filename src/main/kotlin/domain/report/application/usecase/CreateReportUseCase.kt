package com.peekr.domain.report.application.usecase

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.exception.common.CommonException
import com.peekr.common.model.UserId
import com.peekr.domain.report.application.dto.ReportDto
import com.peekr.domain.report.domain.model.Report
import com.peekr.domain.report.domain.repository.ReportRepository

/**
 * 신고 생성 유스케이스
 *
 * 사용자가 신고 접수 시 해당 유스케이스를 사용한다.
 */
class CreateReportUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(
        ownerId: UserId,
        reportDto: ReportDto,
    ) {
        if (ownerId.value != reportDto.reporterId) {
            throw CommonException.AccessDenied()
        }
        val reporterId = UserId(reportDto.reporterId)
        val reportedId = UserId(reportDto.reportedId)
        val reasonId = ReportReasonId(reportDto.reasonId)
        val report = Report(reporterId, reportedId, reasonId, reportDto.customReason)
        reportRepository.createReport(report)
    }
}
