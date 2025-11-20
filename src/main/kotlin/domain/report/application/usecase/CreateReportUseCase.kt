package com.peekr.domain.report.application.usecase

import com.peekr.common.exception.common.CommonException
import com.peekr.common.model.ReportReasonId
import com.peekr.common.model.UserId
import com.peekr.domain.report.application.dto.ReportDetailDto
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.domain.report.domain.repository.ReportRepository

/**
 * 신고 생성 유스케이스
 *
 * 사용자가 신고 접수 시 해당 유스케이스를 사용한다.
 */
class CreateReportUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(
        ownerId: UserId,
        reportDetailDto: ReportDetailDto,
    ) {
        if (ownerId.value != reportDetailDto.reporterId) {
            throw CommonException.AccessDenied()
        }
        val reporterId = UserId(reportDetailDto.reporterId)
        val reportedId = UserId(reportDetailDto.reportedId)
        val reasonId = ReportReasonId(reportDetailDto.reasonId)
        val report = ReportDetail(reporterId, reportedId, reasonId, reportDetailDto.customReason)
        reportRepository.createReport(report)
    }
}
