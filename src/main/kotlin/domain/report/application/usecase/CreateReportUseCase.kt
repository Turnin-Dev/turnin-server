package com.peekr.domain.report.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.exception.common.CommonException
import com.peekr.common.model.id.ReportReasonId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.application.dto.ReportDetailDto
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.exception.ReportException

/**
 * 신고 생성
 *
 * 사용자가 신고 접수 시 해당 유스케이스를 사용한다.
 *
 * @throws [DatabaseException.DuplicatedDataException] 중복 신고 시 예외 발생
 * @throws [ReportException.MissingReportTargetException] 필수 신고 대상이 빠져있는 경우 예외 발생
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
        val reportedId = reportDetailDto.reportedId?.let { UserId(it) }
        val reportedUserKeywordId = reportDetailDto.reportedUserKeywordId?.let { UserKeywordId(it) }
        val reasonId = ReportReasonId(reportDetailDto.reasonId)
        val reportDetail = ReportDetail.create(
            reporterId = reporterId,
            reportedId = reportedId,
            reportedUserKeywordId = reportedUserKeywordId,
            reasonId = reasonId,
            customReason = reportDetailDto.customReason,
        )
        reportRepository.createReport(reportDetail)
    }
}
