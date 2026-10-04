package com.turnin.domain.contentReport.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.contentReport.application.dto.ContentReportDto
import com.turnin.domain.contentReport.domain.model.ContentReportDetail
import com.turnin.domain.contentReport.domain.model.ContentReportPolicy
import com.turnin.domain.contentReport.domain.provider.ReportableContentProvider
import com.turnin.domain.contentReport.domain.repository.ContentReportRepository
import com.turnin.domain.contentReport.exception.ContentReportException

/**
 * 콘텐츠 신고 생성
 *
 * 신고 시점의 콘텐츠 내용을 스냅샷으로 함께 저장하며, 신고 누적 횟수가 운영 정책 기준([ContentReportPolicy]) 이상이 되면 콘텐츠를 숨김 처리한다.
 *
 * 차단 관계여도 차단 전에 노출된 콘텐츠는 신고할 수 있다.
 *
 * @throws [ValidatorException] 콘텐츠 ID 또는 신고 사유 ID가 0 이하인 경우
 * @throws [ContentReportException.ContentNotFound] 신고할 콘텐츠가 없거나, 이미 숨김 처리된 경우
 * @throws [ContentReportException.CannotReportOwnContent] 본인이 작성한 콘텐츠를 신고하려는 경우
 * @throws [ContentReportException.AlreadyReported] 이미 신고한 콘텐츠인 경우
 * @throws [ContentReportException.InvalidReportReason] 존재하지 않는 신고 사유인 경우
 */
class CreateContentReportUseCase(
    private val contentReportRepository: ContentReportRepository,
    private val reportableContentProvider: ReportableContentProvider,
) {
    /**
     * @param reporterId 신고자(요청자) ID
     * @param contentReportDto 콘텐츠 신고 DTO
     */
    suspend operator fun invoke(
        reporterId: UserId,
        contentReportDto: ContentReportDto,
    ) {
        val contentType = contentReportDto.contentType
        val contentId = contentReportDto.contentId
        val reasonId = ReportReasonId(contentReportDto.reasonId)

        LOGGER.info(
            message = "Content report attempt: reporter=$reporterId, contentType=$contentType, contentId=$contentId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REPORT_ATTEMPT.value,
                LogTag.USER_ID.key to reporterId.value.toString(),
            ),
        )

        // 신고 저장과 누적 횟수 집계, 숨김 처리를 하나의 트랜잭션으로 처리한다.
        val isHidden = suspendTransaction {
            val reportableContent = reportableContentProvider.findVisible(contentType, contentId)
                ?: throw ContentReportException.ContentNotFound()
            val contentReportDetail = ContentReportDetail.create(
                reporterId = reporterId,
                reportedContent = reportableContent,
                contentType = contentType,
                contentId = contentId,
                reasonId = reasonId,
                customReason = contentReportDto.customReason,
            )

            try {
                contentReportRepository.create(contentReportDetail)
            } catch (e: DatabaseException.DuplicatedDataException) {
                throw ContentReportException.AlreadyReported(e)
            } catch (e: DatabaseException.ForeignKeyViolationException) {
                throw ContentReportException.InvalidReportReason(e)
            }

            val reportCount = contentReportRepository.countByContent(contentType, contentId)
            ContentReportPolicy.shouldHide(reportCount).also { shouldHide ->
                if (shouldHide) {
                    reportableContentProvider.hide(contentType, contentId)
                }
            }
        }

        LOGGER.info(
            message = "Content reported successfully: contentType=$contentType, contentId=$contentId, hidden=$isHidden",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REPORT_SUCCESS.value,
                LogTag.USER_ID.key to reporterId.value.toString(),
                "report_reason_id" to reasonId.value.toString(),
            ),
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateContentReportUseCase>()
