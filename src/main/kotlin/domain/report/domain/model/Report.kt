package com.peekr.domain.report.domain.model

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.exception.DomainException
import com.peekr.common.model.ReportId
import com.peekr.common.model.UserId

class ReportDomainException(message: String) : DomainException(message)

/**
 * 신고 엔티티 모델
 *
 * @property id 신고 ID
 * @property reporterId 신고자 ID
 * @property reportedId 피신고자 ID
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
class Report private constructor(
    val id: ReportId,
    val reporterId: UserId,
    val reportedId: UserId,
    val reasonId: ReportReasonId,
    val customReason: String?,
) {
    companion object {
        operator fun invoke(
            reporterId: UserId,
            reportedId: UserId,
            reasonId: ReportReasonId,
            customReason: String? = null,
        ): Report {
            // 1) 신고자와 피신고자가 같으면 안된다.
            if (reporterId == reportedId) {
                throw ReportDomainException("본인을 신고할 수 없습니다.")
            }

            return Report(
                id = ReportId.UNSAVED,
                reporterId = reporterId,
                reportedId = reportedId,
                reasonId = reasonId,
                customReason = customReason,
            )
        }
    }
}
