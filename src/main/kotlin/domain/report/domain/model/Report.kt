package com.peekr.domain.report.domain.model

import com.peekr.common.exception.DomainException
import com.peekr.common.model.ReportId
import org.flywaydb.core.internal.reports.ReportDetails

class ReportDomainException(message: String) : DomainException(message)

/**
 * 신고 엔티티 모델
 *
 * @property id 신고 ID
 * @property details 신고 디테일 엔티티 모델
 */
data class Report(
    val id: ReportId,
    val details: ReportDetails,
)
