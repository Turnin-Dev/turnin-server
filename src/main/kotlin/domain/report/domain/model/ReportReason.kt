package com.peekr.domain.report.domain.model

import com.peekr.common.model.ReportReasonId

/**
 * 신고 사유 엔티티 모델
 *
 * @property id 신고 사유 ID
 * @property code 신고 사유 코드
 * @property description 신고 사유 설명
 */
data class ReportReason(
    val id: ReportReasonId,
    val code: String,
    val description: String,
)
