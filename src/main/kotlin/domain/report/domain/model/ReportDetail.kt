package com.peekr.domain.report.domain.model

import com.peekr.common.model.id.ReportReasonId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.exception.ReportException

/**
 * 신고 디테일 엔티티 모델
 *
 * @property reporterId 신고자 ID
 * @property reportedId 신고 대상 사용자 ID
 * @property reportedUserKeywordId 신고 대상 사용자 키워드 ID
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
data class ReportDetail(
    val reporterId: UserId,
    val reportedId: UserId?,
    val reportedUserKeywordId: UserKeywordId?,
    val reasonId: ReportReasonId,
    val customReason: String?,
) {
    companion object {
        /**
         * @throws ReportException 비즈니스 규칙 위반 시 예외가 발생한다.
         */
        fun create(
            reporterId: UserId,
            reportedId: UserId?,
            reportedUserKeywordId: UserKeywordId?,
            reasonId: ReportReasonId,
            customReason: String? = null,
        ): ReportDetail {
            // 1) 신고자와 피신고자가 같으면 안된다.
            if (reporterId == reportedId) {
                throw ReportException.CannotReportMySelf()
            }

            // 2) 신고 대상은 반드시 1개 이상이어야 한다.
            if (reportedId == null && reportedUserKeywordId == null) {
                throw ReportException.MissingReportTargetException()
            }

            return ReportDetail(
                reporterId = reporterId,
                reportedId = reportedId,
                reportedUserKeywordId = reportedUserKeywordId,
                reasonId = reasonId,
                customReason = customReason,
            )
        }
    }
}
