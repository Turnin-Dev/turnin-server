package com.turnin.domain.report.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.exception.common.CommonException
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.report.application.dto.ReportDetailDto
import com.turnin.domain.report.domain.model.ReportDetail
import com.turnin.domain.report.domain.repository.ReportRepository
import com.turnin.domain.report.exception.ReportException

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

        LOGGER.info(
            message = "Report creation attempt: " +
                "reporter=$reporterId, targetUser=$reportedId, targetKeyword=$reportedUserKeywordId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REPORT_ATTEMPT.value,
                LogTag.USER_ID.key to reporterId.value.toString(),
            ),
        )

        val reportDetail = ReportDetail.create(
            reporterId = reporterId,
            reportedId = reportedId,
            reportedUserKeywordId = reportedUserKeywordId,
            reasonId = reasonId,
            customReason = reportDetailDto.customReason,
        )
        reportRepository.createReport(reportDetail)

        LOGGER.info(
            message = "Report created successfully: reasonId=${reasonId.value}",
            tags = mutableMapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REPORT_SUCCESS.value,
                LogTag.USER_ID.key to reporterId.value.toString(),
                "report_reason_id" to reasonId.value.toString(),
            ).apply {
                // 신고 대상에 따라 태그 동적 추가
                reportedId?.let { put("reported_user_id", it.value.toString()) }
                reportedUserKeywordId?.let { put("reported_keyword_id", it.value.toString()) }
            },
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateReportUseCase>()
