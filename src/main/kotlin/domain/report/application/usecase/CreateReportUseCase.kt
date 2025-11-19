package com.peekr.domain.report.application.usecase

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.model.UserId
import com.peekr.domain.report.domain.model.Report
import com.peekr.domain.report.domain.repository.ReportRepository

/**
 * 신고 생성 유스케이스
 *
 * 사용자가 신고 접수 시 해당 유스케이스를 사용한다.
 */
class CreateReportUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(
        reporterId: UserId,
        reportedId: UserId,
        reasonId: ReportReasonId,
        customReason: String? = null,
    ) {
        val report = Report(reporterId, reportedId, reasonId, customReason)
        reportRepository.createReport(report)
    }
}
