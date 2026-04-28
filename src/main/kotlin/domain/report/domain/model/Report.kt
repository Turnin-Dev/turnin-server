package com.turnin.domain.report.domain.model

import com.turnin.common.model.id.ReportId

/**
 * 신고 엔티티 모델
 *
 * @property id 신고 ID
 * @property details 신고 디테일 엔티티 모델
 */
data class Report(
    val id: ReportId,
    val details: ReportDetail,
)
