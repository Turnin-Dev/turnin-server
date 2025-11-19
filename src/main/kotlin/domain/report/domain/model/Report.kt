package com.peekr.domain.report.domain.model

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.model.ReportId
import com.peekr.common.model.UserId

/**
 * 신고 엔티티 모델
 *
 * @property id 신고 ID
 * @property reporterId 신고자 ID
 * @property reportedId 피신고자 ID
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
data class Report(
    val id: ReportId,
    val reporterId: UserId,
    val reportedId: UserId,
    val reasonId: ReportReasonId,
    val customReason: String?,
)
