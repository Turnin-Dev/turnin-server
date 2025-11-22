package com.peekr.domain.report.domain.model

import com.peekr.common.model.id.ReportReasonId
import com.peekr.common.model.id.UserId

/**
 * 신고 디테일 엔티티 모델
 *
 * @property reporterId 신고자 ID
 * @property reportedId 피신고자 ID
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
data class ReportDetail(
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
        ): ReportDetail {
            // 1) 신고자와 피신고자가 같으면 안된다.
            if (reporterId == reportedId) {
                throw ReportDomainException("본인을 신고할 수 없습니다.")
            }

            return ReportDetail(
                reporterId = reporterId,
                reportedId = reportedId,
                reasonId = reasonId,
                customReason = customReason,
            )
        }
    }
}
