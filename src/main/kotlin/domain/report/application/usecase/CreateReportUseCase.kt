package com.peekr.domain.report.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.exception.common.CommonException
import com.peekr.common.model.id.ReportReasonId
import com.peekr.common.model.id.UserId
import com.peekr.domain.report.application.dto.ReportDetailDto
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.domain.report.domain.repository.ReportRepository

/**
 * 신고 생성
 *
 * 사용자가 신고 접수 시 해당 유스케이스를 사용한다.
 *
 * @throws [DatabaseException.DuplicatedDataException] 중복 신고 시 예외 발생
 */
class CreateReportUseCase(private val reportRepository: ReportRepository) {
    /**
     * 신고 생성을 한다.
     *
     * @param ownerId 요청자 ID
     * @param reportDetailDto 신고 디테일 DTO
     */
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
