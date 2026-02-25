package com.peekr.domain.report.application.provider

import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.domain.repository.ReportRepository

/**
 * 외부에 제공할 Report API
 */
class ReportProviderApi(private val reportRepository: ReportRepository) {
    /**
     * 사용자 키워드 ID가 신고 내역에 존재하는지 확인한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     */
    suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean =
        reportRepository.existsByUserKeywordId(userKeywordId)
}
