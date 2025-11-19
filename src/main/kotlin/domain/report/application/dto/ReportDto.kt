package com.peekr.domain.report.application.dto

/**
 * 신고 DTO
 *
 * @param reporterId 신고자 ID
 * @param reportedId 피신고자 ID
 * @param reasonId 신고 사유 ID
 * @param customReason 기타 신고 사유
 */
data class ReportDto(
    val reporterId: Long,
    val reportedId: Long,
    val reasonId: Long,
    val customReason: String? = null,
)
