package com.peekr.domain.report.application.dto

/**
 * 신고 DTO
 *
 * @param reporterId 신고자 ID
 * @param reportedId 신고 대상 사용자 ID
 * @param reportedUserKeywordId 신고 대상 사용자 키워드 ID
 * @param reasonId 신고 사유 ID
 * @param customReason 기타 신고 사유
 */
data class ReportDetailDto(
    val reporterId: Long,
    val reportedId: Long?,
    val reportedUserKeywordId: Long?,
    val reasonId: Long,
    val customReason: String? = null,
)
